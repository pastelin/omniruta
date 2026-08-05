package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps.HEADShowStaffsToClientsMap;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos.HEADNearbyDelta;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos.HEADNearbyWatchReq;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesToProfiles;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADEventsStaffConst.NEARBY_DELTA;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADNearbyService {

    private final HEADShowStaffsToClientsMap nearbyComputer; // usa staffWithinRadiusDtoFastMulti(...)
    private final HEADPackagesToProfilesRepository packagesToProfilesRepo;
    private final HEADPackagesPersonalRepository   packagesRepo;
    private final HEADOccupationsProfilesRepository occupationsProfilesRepo;
    private final HEADWsEmitter emitter; // para toSession(sessionId, event, payload)

    // Config
    private static final long   TICK_MS = 2_000L; // frecuencia del delta
    private static final String V       = "1.0";

    // Watch activo por sesión
    private final ConcurrentHashMap<String, Watch> watches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, r -> {
                Thread t = new Thread(r, "nearby-ticker");
                t.setDaemon(true); return t;
            });

    // --- API pública ---

    /** Inicia el watch; devuelve SNAPSHOT inicial que ya está filtrado/ordenado. */
    public HEADStaffsActivesDto start(String sessionId, HEADNearbyWatchReq req) {
        var hydrated = hydrateFromPackage(req);

        // snapshot inicial
        var snap = computeSnapshot(hydrated);

        var last = snap.getHeadStaffsCurrents().stream()
                .collect(Collectors.toUnmodifiableMap(
                        HEADStaffActiveCurrent::getUuidUser,
                        x -> x,
                        (a,b)->a
                ));

        // ✅ 1) guarda last
        var lastRef = new AtomicReference<Map<String, HEADStaffActiveCurrent>>(last);

        // ✅ 2) EMITE snapshot inicial
        emitter.emitToClient(
                sessionId,
                HEADWsEvents.NEARBY_SNAPSHOT,
                snap
        );

        // ✅ 3) ticker arranca normal
        var task = scheduler.scheduleAtFixedRate(
                () -> tick(sessionId),
                TICK_MS,
                TICK_MS,
                TimeUnit.MILLISECONDS
        );

        var w = new Watch(sessionId, hydrated, lastRef, new AtomicBoolean(true), task);

        watches.put(sessionId, w);

        return snap;
    }


    /** Detiene el watch para la sesión. */
    public void stop(String sessionId) {
        Optional.ofNullable(watches.remove(sessionId))
                .map(Watch::task)
                .ifPresent(f -> f.cancel(false));
    }

    // --- Lógica interna ---

    /** En cada tick recalcula snapshot, genera delta y emite a la sesión. */
    private void tick(String sessionId) {
        var w = watches.get(sessionId);
        if (w == null) return;

        var nowDto = computeSnapshot(w.params());
        var nowMap = nowDto.getHeadStaffsCurrents().stream()
                .collect(Collectors.toUnmodifiableMap(
                        HEADStaffActiveCurrent::getUuidUser, x -> x, (a,b)->a));

        // ✅ primer tick => manda todos como added
        if (w.firstTick().compareAndSet(true, false)) {
            var delta = new HEADNearbyDelta(
                    nowMap.values().stream().toList(),
                    List.of(),
                    List.of()
            );
            w.last().set(nowMap);
            emitter.emitToClient(sessionId, HEADWsEvents.NEARBY_DELTA, delta);
            return;
        }

        var prev = w.last().get();
        var delta = computeDelta(prev, nowMap);

        var changed = !delta.getAdded().isEmpty() || !delta.getRemoved().isEmpty() || !delta.getUpdated().isEmpty();
        if (changed) {
            w.last().set(nowMap);
            emitter.emitToClient(sessionId, HEADWsEvents.NEARBY_DELTA, delta);
        }
    }


    /** Genera el delta sin for/ifs “crudos”; usa streams. */
    private HEADNearbyDelta computeDelta(Map<String, HEADStaffActiveCurrent> prev,
                                        Map<String, HEADStaffActiveCurrent> now) {

        var added = now.keySet().stream()
                .filter(k -> !prev.containsKey(k))
                .map(now::get)
                .toList();

        var removed = prev.keySet().stream()
                .filter(k -> !now.containsKey(k))
                .toList();

        // updated = intersección con cambios relevantes (lat/lng, matched ids, distancia/eta)
        var updated = now.keySet().stream()
                .filter(prev::containsKey)
                .filter(k -> !equivalent(prev.get(k), now.get(k)))
                .map(now::get)
                .toList();

        return new HEADNearbyDelta(added, updated, removed);
    }

    /** Equivalencia “shallow” de campos que importan para render (puedes ajustar). */
    private boolean equivalent(HEADStaffActiveCurrent a, HEADStaffActiveCurrent b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        // compara solo lo que afecta el UI de marker: posición/encabezado/filtros/eta
        return Objects.equals(a.getLatitude(), b.getLatitude())
                && Objects.equals(a.getLongitude(), b.getLongitude())
                && Objects.equals(a.getIdProfileUser(), b.getIdProfileUser())
                && Objects.equals(a.getDistanceKm(), b.getDistanceKm())
                && Objects.equals(a.getEtaMinutes(), b.getEtaMinutes())
                && Objects.equals(a.getMatchedProfileIds(), b.getMatchedProfileIds());
    }

    /** Resuelve perfiles permitidos por packageSlug (si viene) y los agrega a los filtros. */
    private HEADNearbyWatchReq hydrateFromPackage(HEADNearbyWatchReq in) {
        if (in == null) return new HEADNearbyWatchReq();

        // 0) Perfiles desde paquete + request
        final Set<Long> profsFromPkg =
                Optional.ofNullable(in.getPackageSlug())
                        .flatMap(occupationsProfilesRepo::findById)
                        .map(pkg -> packagesToProfilesRepo.findByIdOccupationProfile(pkg).orElse(List.of()))
                        .stream()
                        .flatMap(List::stream)
                        .map(HEADPackagesToProfiles::getIdOccupationProfile)
                        .map(HEADOccupationProfile::getIdOccupationProfile)
                        .collect(Collectors.toUnmodifiableSet());

        final Set<Long> mergedProfiles =
                Stream.concat(
                        Optional.ofNullable(in.getFilterProfileIds()).orElse(Set.of()).stream(),
                        profsFromPkg.stream()
                ).collect(Collectors.toUnmodifiableSet());


        var out = new HEADNearbyWatchReq();
        out.setLat(in.getLat());
        out.setLng(in.getLng());
        out.setLimit(in.getLimit());
        out.setUseEta(in.isUseEta());
        out.setAvgSpeedKmh(in.getAvgSpeedKmh());
        out.setFilterProfileIds(mergedProfiles);
        out.setPackageSlug(in.getPackageSlug());
        return out;
    }

    /** Snapshot “rápido” multi–servicio (usa tu orquestador existente). */
    private HEADStaffsActivesDto computeSnapshot(HEADNearbyWatchReq p) {
        return nearbyComputer.staffWithinRadiusDtoFastMulti(
                p.getLat(),
                p.getLng(),
                p.getLimit(),
                p.isUseEta(),
                p.getAvgSpeedKmh(),
                p.getFilterProfileIds(),
                false
        );
    }

    private String normalizeCode(String s) {
        return Optional.ofNullable(s).map(x -> x.trim().toLowerCase().replace(' ', '_').replace('-', '_'))
                .orElse("");
    }

    // --- Modelo de watch ---
    private record Watch(
            String sessionId,
            HEADNearbyWatchReq params,
            AtomicReference<Map<String, HEADStaffActiveCurrent>> last,
            AtomicBoolean firstTick,
            ScheduledFuture<?> task
    ) {}
}