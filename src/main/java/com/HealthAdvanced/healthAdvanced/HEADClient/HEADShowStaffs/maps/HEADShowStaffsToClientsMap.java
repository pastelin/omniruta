package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.dto.HEADStaffProfileLite;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos.HEADNearbyDelta;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADContracts.HEADFileAssetSystemService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService.kms;
import static com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService.mts;

@Service
public class HEADShowStaffsToClientsMap {
    @Autowired private HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    @Autowired private HEADActiveLocationMapService headActiveLocationMapService;
    @Autowired private HEADStaffStateStore staffStateStore;
    @Autowired private HEADPersonalUserRepository headPersonalUserRepository;
    @Autowired private HEADFileAssetSystemService fileAssetService;

    private static final double METERS_PER_DEG_LAT = 111_320.0;
    private static final long   MAX_STALENESS_MS   = 30_000L;

    private static double round2(double v) {
        return new java.math.BigDecimal(v).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Snapshot para mapa (sin ActiveLocationPersonal).
     * - Bounding box + distancia
     * - Sólo elegibles
     * - Multi-perfil
     * - Filtros por IDs de perfil y/o service codes normalizados
     */
    @Transactional(readOnly = true)
    public HEADStaffsActivesDto staffWithinRadiusDtoFastMulti(
            double centerLat,
            double centerLng,
            int limit,
            boolean useEta,
            double avgSpeedKmh,
            Set<Long> filterProfileIds,
            boolean onlyAvailable   // 👈 NUEVO
    ) {
        final long now = System.currentTimeMillis();

        final double dLat = mts / METERS_PER_DEG_LAT;
        final double dLng = mts / (METERS_PER_DEG_LAT * Math.cos(Math.toRadians(centerLat)));
        final double minLat = centerLat - dLat, maxLat = centerLat + dLat;
        final double minLng = centerLng - dLng, maxLng = centerLng + dLng;

        record Row(String uuid, HEADStaffStateDto s, double meters) {}

        final LinkedHashSet<Long> profIds =
                filterProfileIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(filterProfileIds);
        final boolean hasProfileFilter = !profIds.isEmpty();

        final Set<String> uuidsByProfile = hasProfileFilter
                ? headOccupationPersonalUserRepository.findStaffUidsByProfileIds(profIds)
                : java.util.Collections.emptySet();

        final int max = Math.max(1, Math.min(limit, 200));

        // 🔑 AQUÍ decides la fuente según el uso
        Map<String, HEADStaffStateDto> sourceStates = onlyAvailable
                ? staffStateStore.staffAvailable()   // para asignar jobs
                : staffStateStore.staffOnline();     // para nearby

        var currents = sourceStates.entrySet().stream()

                // filtro por perfil si aplica
                .filter(e -> !hasProfileFilter || uuidsByProfile.contains(e.getKey()))

                // coords válidas + dentro del bbox
                .filter(e -> {
                    var s = e.getValue();
                    if (s.lat() == null || s.lng() == null) return false;
                    return s.lat() >= minLat && s.lat() <= maxLat
                            && s.lng() >= minLng && s.lng() <= maxLng;
                })

                // distancia
                .map(e -> new Row(
                        e.getKey(),
                        e.getValue(),
                        headActiveLocationMapService.distanceKmCeilStep(
                                centerLat, centerLng, e.getValue().lat(), e.getValue().lng(), 0.5
                        )
                ))

                // dentro del radio (ajusta si r.meters son km o m)
                .filter(r -> r.meters() <= mts)

                .sorted(java.util.Comparator.comparingDouble(Row::meters))
                .limit(max)

                .map(r -> {
                    String uuid = r.uuid();
                    var s = r.s();

                    var staff = headPersonalUserRepository.findByUidUser(uuid).orElse(null);
                    if (staff == null) return null;

                    var all = headOccupationPersonalUserRepository
                            .findByIdPersonalUser(staff)
                            .orElse(java.util.List.of());
                    if (all.isEmpty()) return null;

                    var matched = hasProfileFilter
                            ? all.stream()
                            .filter(p -> profIds.contains(
                                    p.getIdOccupationProfile().getIdOccupationProfile()
                            ))
                            .toList()
                            : all;

                    if (matched.isEmpty()) return null;

                    var profileDtos = matched.stream()
                            .map(p -> {
                                var sp = new HEADStaffProfileLite();
                                sp.setIdProfileUser(p.getIdOccupationProfile().getIdOccupationProfile());
                                sp.setIdOccupation(p.getIdOccupationProfile().getIdOccupation().getIdOccupation());
                                sp.setProfileStaff(p.getIdOccupationProfile().getNameTypeProfile());
                                sp.setProfileOccupation(p.getIdOccupationProfile().getIdOccupation().getNameOccupation());

                                String url = fileAssetService
                                        .mapPinStaff(p.getIdOccupationProfile().getIdOccupationProfile())
                                        .orElse(new HEADFileAsset())
                                        .getUrl();

                                sp.setImageUrl(url);
                                return sp;
                            })
                            .collect(java.util.stream.Collectors.toList());

                    var matchedIds = matched.stream()
                            .map(p -> p.getIdOccupationProfile().getIdOccupationProfile())
                            .collect(java.util.stream.Collectors.toList());

                    var header = hasProfileFilter
                            ? profIds.stream()
                            .flatMap(profileId -> matched.stream()
                                    .filter(p -> profileId.equals(
                                            p.getIdOccupationProfile().getIdOccupationProfile()
                                    ))
                                    .limit(1)
                            )
                            .findFirst()
                            .orElse(matched.stream().findFirst().orElse(null))
                            : matched.stream().findFirst().orElse(null);

                    if (header == null) return null;

                    var cur = new HEADStaffActiveCurrent();
                    cur.setIdPersonalUser(staff.getIdUser());
                    cur.setUuidUser(uuid);
                    cur.setIdProfileUser(header.getIdOccupationProfile().getIdOccupationProfile());
                    cur.setProfileStaff(header.getIdOccupationProfile().getNameTypeProfile());
                    cur.setProfileOccupation(header.getIdOccupationProfile().getIdOccupation().getNameOccupation());
                    cur.setLatitude(s.lat());
                    cur.setLongitude(s.lng());
                    cur.setDistanceKm(r.meters());
                    cur.setEtaMinutes(
                            headActiveLocationMapService.calculateTravelMetersTimeInMinutes(r.meters())
                    );
                    cur.setProfiles(profileDtos);
                    cur.setMatchedProfileIds(matchedIds);
                    return cur;
                })

                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        var dto = new HEADStaffsActivesDto();
        dto.setHeadStaffsCurrents(currents);
        return dto;
    }

    private String normalizeCode(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replace(' ', '_').replace('-', '_');
    }

}