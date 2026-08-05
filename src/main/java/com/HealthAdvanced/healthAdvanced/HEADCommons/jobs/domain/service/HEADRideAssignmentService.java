package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesMaps.HEADPackagesMaps;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps.HEADShowStaffsToClientsMap;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service.HEADGeocodingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobSearchStaff;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADOfferDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.mappers.HEADJobMapper;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.JOB_SEARCH_STAFF;


@Slf4j
@Service
@RequiredArgsConstructor
public class HEADRideAssignmentService {

    private final HEADJobQueueStore queueStore;
    private final HEADJobService jobService;
    private final HEADStaffStateStore staffStateStore;
    private final HEADPersonalUserRepository staffRepo;
    private final HEADWsEmitter emitter;
    private final HEADGeocodingService geocodingService;
    private final HEADPackagesPersonalRepository headPackagesPersonalRepository;
    private final HEADJobRepository jobRepository;
    private final HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;

    // refill dinámico
    private final HEADShowStaffsToClientsMap headShowStaffsToClientsMap;
    private final HEADPackagesMaps packageDirectory;
    private final ApplicationEventPublisher appEvents;

    // ---- Config final ----
    private static final long SEARCH_WINDOW_MS          = 5 * 60 * 1000L; // 5 min
    public  static final long OFFER_TIMEOUT_SECONDS     = 60;             // 60s para aceptar
    private static final long REFRESH_TICK_SECONDS      = 3;              // cada 3s revisa/refill

    private static final int  MAX_ATTEMPTS_PER_STAFF    = 2;
    private static final long COOLDOWN_AFTER_TIMEOUT_MS = 120_000L; // 2m
    private static final long COOLDOWN_AFTER_REJECT_MS  = 300_000L; // 5m
    private static final long COOLDOWN_NOT_ELIGIBLE_MS  = 30_000L;  // 30s
    private static final long SCHEDULE_PENDING_TIMEOUT_SECONDS = 5 * 60; // 5 min
    private static final long DECISION_TIMEOUT_SECONDS = 60;

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> decisionTimeouts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> decisionHolder = new ConcurrentHashMap<>();


    private final ConcurrentHashMap<Long, ScheduledFuture<?>> schedulePendingTimeouts = new ConcurrentHashMap<>();


    // pool > 1 hilo para que tick/timeout no se “atoren”
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> globalTimeouts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> offerTimeouts  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> refreshTicks   = new ConcurrentHashMap<>();

    // jobId -> staffUuid ofrecido actualmente (si existe)
    private final ConcurrentHashMap<Long, String> currentOfferedStaff = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> schedulePendingStaff = new ConcurrentHashMap<>();

    // -------------------------
    // ENTRY POINT
    // -------------------------
    public void startAssignmentProcess(Long jobId, List<String> initialQueue) {

        log.warn("[ASSIGN_START] jobId={} initialQueueSize={} initialQueue={}",
                jobId,
                (initialQueue == null ? 0 : initialQueue.size()),
                initialQueue);

        if (jobId == null) {
            log.error("[ASSIGN_START] jobId is null - abort");
            return;
        }

        // init session (5 min)
        queueStore.initSession(jobId, (initialQueue == null ? List.of() : initialQueue), SEARCH_WINDOW_MS);

        var s = queueStore.get(jobId);
        log.warn("[ASSIGN_SESSION] jobId={} createdAtMs={} endsAtMs={} queueSize={}",
                jobId,
                (s != null ? s.createdAtMs() : null),
                (s != null ? s.endsAtMs() : null),
                (s != null && s.queue() != null ? s.queue().size() : null));

        emitSearchStarted(jobId);

        scheduleRefreshTick(jobId);

        offerNextDriver(jobId);

        scheduleGlobalTimeout(jobId);
    }

    // -------------------------
    // GLOBAL TIMEOUT (5 min)
    // -------------------------
    private void scheduleGlobalTimeout(Long jobId) {
        Optional.ofNullable(globalTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        log.warn("[GLOBAL_TIMEOUT_SCHEDULE] jobId={} delayMs={}", jobId, SEARCH_WINDOW_MS);

        var future = scheduler.schedule(() -> {
            try {
                log.warn("[GLOBAL_TIMEOUT_FIRE] jobId={}", jobId);
                // al disparar, cancelamos directo (no dependemos de isExpired)
                cancelAssignmentProcess(jobId, "SEARCH_TIMEOUT");
            } catch (Exception e) {
                log.error("[GLOBAL_TIMEOUT] jobId={} err={}", jobId, e.getMessage(), e);
            }
        }, SEARCH_WINDOW_MS, TimeUnit.MILLISECONDS);

        globalTimeouts.put(jobId, future);
    }

    // -------------------------
    // REFRESH TICK (detecta staff nuevo)
    // -------------------------
    private void scheduleRefreshTick(Long jobId) {
        Optional.ofNullable(refreshTicks.remove(jobId)).ifPresent(f -> f.cancel(false));

        log.warn("[REFRESH_TICK_SCHEDULE] jobId={} everySec={}", jobId, REFRESH_TICK_SECONDS);

        var future = scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean expired = queueStore.isExpired(jobId);
                String offered = currentOfferedStaff.get(jobId);
                boolean queueEmpty = queueStore.isQueueEmpty(jobId);

                log.info("[TICK] jobId={} expired={} offeredStaff={} queueEmpty={}",
                        jobId, expired, offered, queueEmpty);

                if (expired) {
                    log.warn("[TICK] jobId={} session expired -> stop tick", jobId);
                    stopRefreshTick(jobId);
                    return;
                }

                // si hay oferta activa, no hacemos nada
                if (offered != null) return;

                // si ya hay cola, intenta ofertar
                if (!queueEmpty) {
                    offerNextDriver(jobId);
                    return;
                }

                // cola vacía: refill dinámico
                refillCandidates(jobId);

                // si después del refill hay cola, ofertar
                if (!queueStore.isQueueEmpty(jobId)) offerNextDriver(jobId);

            } catch (Exception e) {
                log.error("[REFRESH_TICK] jobId={} err={}", jobId, e.getMessage(), e);
            }
        }, 0, REFRESH_TICK_SECONDS, TimeUnit.SECONDS);

        refreshTicks.put(jobId, future);
    }

    private void stopRefreshTick(Long jobId) {
        Optional.ofNullable(refreshTicks.remove(jobId)).ifPresent(f -> f.cancel(false));
        log.warn("[REFRESH_TICK_STOP] jobId={}", jobId);
    }

    private void refillCandidates(Long jobId) {

        var v = jobRepository.findRefillView(jobId);
        if (v == null) return;

        var mode = v.getServiceMode();
        String pkgId = v.getPkgId();
        var profileFilter = packageDirectory.resolveProfileIdsFromPackage(pkgId);

        List<String> candidates;

        if (mode == HEADServiceMode.VIDEO) {
            candidates = staffAvailableByProfiles(profileFilter); // global
        } else {
            candidates = nearbyByRadius(v.getClientLat(), v.getClientLng(), profileFilter); // local
        }

        if (!candidates.isEmpty()) {
            queueStore.mergeCandidates(jobId, candidates, MAX_ATTEMPTS_PER_STAFF);
        }
    }


    private List<String> nearbyByRadius(Double lat, Double lng, Set<Long> profileFilter) {

        if (lat == null || lng == null) {
            log.warn("[NEARBY] skip lat/lng null lat={} lng={}", lat, lng);
            return List.of();
        }

        log.info("[NEARBY] lat={} lng={} profileFilterSize={} profileFilter={}",
                lat, lng,
                (profileFilter == null ? 0 : profileFilter.size()),
                profileFilter);

        var nearby = headShowStaffsToClientsMap.staffWithinRadiusDtoFastMulti(
                lat, lng,
                80,          // limit
                true,        // useEta
                25d,         // avgSpeedKmh
                (profileFilter == null ? Set.of() : profileFilter),
                true         // onlyAvailable (online && !busy && !hasServiceRequest)
        );

        if (nearby == null || nearby.getHeadStaffsCurrents() == null) {
            log.info("[NEARBY] no result (nearby null)");
            return List.of();
        }

        var candidates = nearby.getHeadStaffsCurrents().stream()
                .map(HEADStaffActiveCurrent::getUuidUser)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.info("[NEARBY] candidatesFound={} candidates={}", candidates.size(), candidates);

        return candidates;
    }


    private List<String> staffAvailableByProfiles(Set<Long> profileFilter) {

        var available = staffStateStore.staffAvailable().keySet(); // uuids

        if (profileFilter == null || profileFilter.isEmpty()) {
            return available.stream().toList();
        }

        var uuidsByProfile = headOccupationPersonalUserRepository.findStaffUidsByProfileIds(profileFilter);

        return available.stream()
                .filter(uuidsByProfile::contains)
                .distinct()
                .toList();
    }


    // -------------------------
    // OFFER LOOP
    // -------------------------
    public void offerNextDriver(Long jobId) {

        if (queueStore.isExpired(jobId)) {
            log.warn("[ASSIGN] jobId={} session expired -> cancel", jobId);
            cancelAssignmentProcess(jobId, "SEARCH_TIMEOUT");
            return;
        }

        // si hay oferta activa, no enviar otra
        if (currentOfferedStaff.containsKey(jobId)) {
            log.info("[ASSIGN] jobId={} skip (offer active) offeredStaff={}",
                    jobId, currentOfferedStaff.get(jobId));
            return;
        }

        String nextUuid = queueStore.pollNext(jobId, MAX_ATTEMPTS_PER_STAFF);

        log.info("[ASSIGN] jobId={} pollNext={}", jobId, nextUuid);

        // si no hay candidatos ahora: espera al tick
        if (nextUuid == null) {
            log.info("[ASSIGN] jobId={} no candidates now (waiting tick)", jobId);
            return;
        }

        if (!staffStateStore.isEligible(nextUuid)) {
            log.info("[ASSIGN] jobId={} next={} not eligible -> cooldown {}ms",
                    jobId, nextUuid, COOLDOWN_NOT_ELIGIBLE_MS);

            queueStore.cooldown(jobId, nextUuid, COOLDOWN_NOT_ELIGIBLE_MS);

            // intenta inmediatamente con el siguiente
            offerNextDriver(jobId);
            return;
        }

        startOfferCycle(jobId, nextUuid);
    }

    private void startOfferCycle(Long jobId, String staffUuid) {
        log.warn("[OFFER_START] jobId={} staffUuid={}", jobId, staffUuid);

        currentOfferedStaff.put(jobId, staffUuid);

        staffRepo.findByUidUser(staffUuid)
                .map(staff -> jobService.offerJobToStaff(jobId, staff)) // TX adentro
                .ifPresentOrElse(offeredJob -> {

                    // bandera en memoria
                    staffStateStore.hasServiceRequest(staffUuid, jobId);

                    var v = jobRepository.findRefillView(jobId);
                    String pkgId = (v != null ? v.getPkgId() : null);

                    var pkg = (pkgId == null)
                            ? null
                            : headPackagesPersonalRepository.findById(pkgId).orElse(null);

                    String addr = safeAddr(offeredJob.getClientLat(), offeredJob.getClientLng());

                    log.warn("[OFFER_EMIT] jobId={} -> staffUuid={} pkgId={} addrNull={}",
                            jobId, staffUuid, pkgId, (addr == null));

                    emitter.emitOffer(staffUuid, HEADOfferDto.of(offeredJob, pkg, addr, addr));

                    scheduleOfferTimeout(jobId, staffUuid);

                }, () -> {
                    log.warn("[OFFER_FAIL] jobId={} staffUuid={} (db offer failed) -> next", jobId, staffUuid);
                    currentOfferedStaff.remove(jobId);
                    staffStateStore.releaseOffer(staffUuid);
                    offerNextDriver(jobId);
                });
    }

    private void scheduleOfferTimeout(Long jobId, String staffUuid) {
        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        var future = scheduler.schedule(() -> {
            try {
                var st = staffStateStore.get(staffUuid);

                boolean stillPending = Optional.ofNullable(st)
                        .map(s -> s.hasServiceRequest() && Objects.equals(s.currentJobId(), jobId))
                        .orElse(false);

                if (!stillPending) return;

                // Ya no expirar el job completo
                jobService.transition(jobId, HEADJobState.PENDING_ASSIGNMENT);

                staffStateStore.releaseOffer(staffUuid);
                currentOfferedStaff.remove(jobId);

                queueStore.exclude(jobId, staffUuid);
                queueStore.cooldown(jobId, staffUuid, COOLDOWN_AFTER_TIMEOUT_MS);

                offerNextDriver(jobId);

            } catch (Exception e) {
                log.error("[OFFER_TIMEOUT] jobId={} staff={} err={}", jobId, staffUuid, e.getMessage(), e);
            }
        }, OFFER_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        offerTimeouts.put(jobId, future);
    }

    // -------------------------
    // STAFF RESPONSES
    // -------------------------

    /** Llamar cuando el staff RECHAZA explícitamente */
    public void onStaffRejected(Long jobId, String staffUuid) {
        log.warn("[STAFF_REJECT] jobId={} staffUuid={}", jobId, staffUuid);

        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        currentOfferedStaff.remove(jobId);

        staffStateStore.rejectedService(staffUuid, jobId);
        staffStateStore.releaseOffer(staffUuid);

        jobService.transition(jobId, HEADJobState.PENDING_ASSIGNMENT);

        queueStore.exclude(jobId, staffUuid);
        queueStore.cooldown(jobId, staffUuid, COOLDOWN_AFTER_REJECT_MS);

        offerNextDriver(jobId);
    }


    /** Llamar cuando el staff ACEPTA: detiene timers + limpia sesión */
    public void onStaffAccepted(Long jobId, String staffUuid) {

        if (queueStore.isExcluded(jobId, staffUuid)) {
            log.warn("[STAFF_ACCEPT] ignored: excluded jobId={} staff={}", jobId, staffUuid);
            return;
        }

        log.warn("[STAFF_ACCEPT] jobId={} staffUuid={}", jobId, staffUuid);

        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(globalTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(refreshTicks.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(schedulePendingTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        currentOfferedStaff.remove(jobId);
        schedulePendingStaff.remove(jobId);
        decisionHolder.remove(jobId);

        queueStore.clear(jobId);

        // NO liberar offer aquí:
        // acceptByStaff(...) ya dejó al staff asignado al job en Redis/estado

        log.warn("[STAFF_ACCEPT_DONE] jobId={} staffUuid={} sessionCleared=true", jobId, staffUuid);
    }

    public void onDecisionOpened(Long jobId, String staffUuid) {
        log.warn("[DECISION_OPENED] jobId={} staffUuid={}", jobId, staffUuid);

        if (jobId == null || staffUuid == null) {
            return;
        }

        var job = jobService.findById(jobId);
        if (job == null) {
            log.warn("[DECISION_OPENED] jobId={} ignore (job not found)", jobId);
            return;
        }

        if (!Objects.equals(job.getStaffUuid(), staffUuid)) {
            log.warn("[DECISION_OPENED] jobId={} ignore (not owner) owner={} caller={}",
                    job.getId(), job.getStaffUuid(), staffUuid);
            return;
        }

        // Aquí todavía sigue OFFERED
        if (job.getState() != HEADJobState.OFFERED) {
            log.warn("[DECISION_OPENED] jobId={} ignore (state={})", jobId, job.getState());
            return;
        }

        // Ya no aplica el timeout de oferta normal
        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        // Ventana de decisión: OFFERED -> ACCEPTED_AWAITING_START
        cancelDecisionTimeout(jobId);
        jobService.acceptAndAwaitStartDecision(jobId, staffUuid);

        decisionHolder.put(jobId, staffUuid);
        scheduleDecisionTimeout(jobId, staffUuid);

        log.warn("[DECISION_OPENED] jobId={} staffUuid={} decision window started", jobId, staffUuid);
    }

    private void scheduleDecisionTimeout(Long jobId, String staffUuid) {
        Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        log.warn("[DECISION_TIMEOUT_SCHEDULE] jobId={} staffUuid={} inSec={}",
                jobId, staffUuid, DECISION_TIMEOUT_SECONDS);

        var future = scheduler.schedule(() -> {
            try {
                String holder = decisionHolder.get(jobId);
                if (!Objects.equals(holder, staffUuid)) {
                    log.warn("[DECISION_TIMEOUT] jobId={} ignore (holder changed) holder={}", jobId, holder);
                    return;
                }

                var job = jobService.findById(jobId);
                if (job == null) {
                    log.warn("[DECISION_TIMEOUT] jobId={} ignore (job not found)", jobId);
                    return;
                }

                // Debe coincidir con el estado real de la ventana de decisión
                if (job.getState() != HEADJobState.ACCEPTED_AWAITING_START) {
                    log.warn("[DECISION_TIMEOUT] jobId={} ignore (state={})", jobId, job.getState());
                    return;
                }

                log.warn("[DECISION_TIMEOUT_FIRE] jobId={} staffUuid={}", jobId, staffUuid);

                // 1) Liberar completamente al staff para que vuelva a ser elegible
                //    clearJob() es mejor que releaseOffer() aquí, porque después de aceptar
                //    el staff ya venía en busy=true/currentJobId=jobId.
                staffStateStore.clearJob(staffUuid);

                // 2) Limpiar holders/timers locales
                currentOfferedStaff.remove(jobId);
                decisionHolder.remove(jobId);
                Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

                // 3) Regresar el job a reasignación
                jobService.transition(jobId, HEADJobState.PENDING_ASSIGNMENT);

                // 4) Avisar al cliente que se buscará otro personal
                emitter.emitToClient(
                        job.getClient().getUuIdUser(),
                        JOB_SEARCH_STAFF,
                        new HEADJobSearchStaff(
                                jobId,
                                "El personal no confirmó a tiempo. Buscando otro personal disponible..."
                        )
                );

                // 5) Reiniciar búsqueda con sesión nueva
                //    Importante: aplicamos cooldown en la nueva sesión ANTES de ofertar.
                queueStore.initSession(jobId, List.of(), SEARCH_WINDOW_MS);
                queueStore.cooldown(jobId, staffUuid, COOLDOWN_AFTER_TIMEOUT_MS);

                emitSearchStarted(jobId);
                scheduleRefreshTick(jobId);
                offerNextDriver(jobId);
                scheduleGlobalTimeout(jobId);

            } catch (Exception e) {
                log.error("[DECISION_TIMEOUT] jobId={} staff={} err={}",
                        jobId, staffUuid, e.getMessage(), e);
            } finally {
                decisionHolder.remove(jobId, staffUuid);
                Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
            }
        }, DECISION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        decisionTimeouts.put(jobId, future);
    }

    public void cancelDecisionTimeout(Long jobId) {
        Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        decisionHolder.remove(jobId);
        log.warn("[DECISION_TIMEOUT_CANCEL] jobId={}", jobId);
    }

    // -------------------------
    // CANCEL / FINAL
    // -------------------------
    @Transactional
    private void cancelAssignmentProcess(Long jobId, String reason) {
        log.warn("[CANCEL_ASSIGN_BEGIN] jobId={} reason={}", jobId, reason);

        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(globalTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(refreshTicks.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(decisionTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(schedulePendingTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        String offered = currentOfferedStaff.remove(jobId);
        if (offered != null) {
            staffStateStore.releaseOffer(offered);
        }

        String pending = schedulePendingStaff.remove(jobId);
        if (pending != null) {
            staffStateStore.releaseOffer(pending);
        }

        String decision = decisionHolder.remove(jobId);
        if (decision != null) {
            staffStateStore.releaseOffer(decision);
        }

        queueStore.clear(jobId);

        jobService.cancelBySystem(
                jobId,
                HEADCancelReason.SEARCH_TIMEOUT,
                reason
        );

        // aquí NO mandar JOB_SEARCH_STAFF
        // el front debe recibir el cambio de estado real: CANCELLED

        log.warn("[CANCEL_ASSIGN_END] jobId={} reason={}", jobId, reason);
    }

    @Transactional
    private void emitSearchStarted(Long jobId) {
        var s = queueStore.get(jobId);
        long endsAt = (s != null ? s.endsAtMs() : (System.currentTimeMillis() + SEARCH_WINDOW_MS));

        var jobCurrent = jobRepository.findJobUuids(jobId).orElseThrow(() -> new HEADBadRequestException("No existe ningun servicio activo"));
        log.warn("[SEARCH_STARTED_EMIT] jobId={} endsAtMs={}", jobId, endsAt);
        emitter.emitToClient(jobCurrent.getClientUuid(), JOB_SEARCH_STAFF, new HEADJobSearchStaff(jobId, "Buscando personal (máx 5 min). Termina: " + endsAt));
    }

    private String safeAddr(double lat, double lng) {
        try {
            return geocodingService.getAddressDescription(lat, lng);
        } catch (Exception e) {
            log.warn("[GEO] failed lat={} lng={} err={}", lat, lng, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void onStaffScheduleCancel(Long jobId, String staffUuid) {
        log.warn("[SCHED_CANCEL] jobId={} staffUuid={}", jobId, staffUuid);

        // 1) cortar timers
        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(schedulePendingTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false)); // ✅

        // 2) liberar holder de pending (si lo guardas)
        schedulePendingStaff.remove(jobId); // ✅

        // 3) DB: regresa a OFFERED
        var job = jobService.transition(jobId, HEADJobState.OFFERED);

        // 4) liberar candados en memoria
        currentOfferedStaff.remove(jobId);
        if (staffUuid != null) staffStateStore.releaseOffer(staffUuid);

        // 5) ✅ NO volver a ofertar ese job al mismo staff
        queueStore.exclude(jobId, staffUuid);

        emitter.emitToClient(job.getClient().getUuIdUser(), JOB_SEARCH_STAFF, new HEADJobSearchStaff(jobId, "Buscando personal..."));
        // 6) ✅ reiniciar assignment completo (no solo offerNextDriver)
        startAssignmentProcess(jobId, List.of()); // ✅
    }

    public void onStaffSchedulePending(Long jobId, String staffUuid) {
        log.warn("[SCHED_PENDING_START] jobId={} staffUuid={}", jobId, staffUuid);

        // 1) Detener el timeout de oferta de 60s (porque ya no aplica)
        Optional.ofNullable(offerTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        // 2) Detener global/tick (para NO seguir ofertando a otros)
        Optional.ofNullable(globalTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        Optional.ofNullable(refreshTicks.remove(jobId)).ifPresent(f -> f.cancel(false));

        // 3) Liberar "oferta activa" del loop
        currentOfferedStaff.remove(jobId);

        // 4) Limpiar sesión de cola (ya no vamos a ofrecer hasta que expire o confirme)
        queueStore.clear(jobId);

        // 5) Marcar que el staff está “en trámite” (para que no se le oferten cosas si tu lógica lo respeta)
        if (staffUuid == null) return;

        staffStateStore.hasServiceRequest(staffUuid, jobId);

        schedulePendingStaff.put(jobId, staffUuid);

        // 6) Programar timeout de SCHEDULE_PENDING
        scheduleSchedulePendingTimeout(jobId, staffUuid);
    }

    @Transactional
    private void scheduleSchedulePendingTimeout(Long jobId, String staffUuid) {
        Optional.ofNullable(schedulePendingTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));

        log.warn("[SCHED_PENDING_TIMEOUT_SCHEDULE] jobId={} staffUuid={} inSec={}",
                jobId, staffUuid, SCHEDULE_PENDING_TIMEOUT_SECONDS);

        var future = scheduler.schedule(() -> {
            try {
                String holder = schedulePendingStaff.get(jobId);
                if (!Objects.equals(holder, staffUuid)) {
                    log.warn("[SCHED_PENDING_TIMEOUT] jobId={} ignore (holder changed) holder={}", jobId, holder);
                    return;
                }

                var job = jobService.findById(jobId);
                if (job == null) {
                    log.warn("[SCHED_PENDING_TIMEOUT] jobId={} ignore (job not found)", jobId);
                    return;
                }

                if (job.getState() != HEADJobState.SCHEDULE_PENDING) {
                    log.warn("[SCHED_PENDING_TIMEOUT] jobId={} ignore (state={})", jobId, job.getState());
                    return;
                }

                log.warn("[SCHED_PENDING_TIMEOUT_FIRE] jobId={} staffUuid={}", jobId, staffUuid);

                // 1) liberar al staff
                staffStateStore.releaseOffer(staffUuid);
                currentOfferedStaff.remove(jobId);
                schedulePendingStaff.remove(jobId);

                // 2) regresar el job a reasignación
                // si tu state machine lo permite, PENDING_ASSIGNMENT es más correcto que OFFERED
                jobService.transition(jobId, HEADJobState.PENDING_ASSIGNMENT);

                // 3) avisar al cliente
                emitter.emitToClient(
                        job.getClient().getUuIdUser(),
                        JOB_SEARCH_STAFF,
                        new HEADJobSearchStaff(
                                jobId,
                                "El personal no respondió a tiempo. Intentaremos asignar a otro personal."
                        )
                );

                // 4) reiniciar búsqueda completa
                startAssignmentProcess(jobId, List.of());

                // 5) IMPORTANTE:
                // startAssignmentProcess crea una sesión nueva, así que la exclusión/cooldown
                // debe aplicarse DESPUÉS para que no vuelva a caerle el mismo staff
                if (staffUuid != null) {
                    queueStore.exclude(jobId, staffUuid);
                    queueStore.cooldown(jobId, staffUuid, COOLDOWN_AFTER_TIMEOUT_MS);
                }

            } catch (Exception e) {
                log.error("[SCHED_PENDING_TIMEOUT] jobId={} staff={} err={}", jobId, staffUuid, e.getMessage(), e);
            } finally {
                schedulePendingStaff.remove(jobId, staffUuid);
                schedulePendingTimeouts.remove(jobId);
            }

        }, SCHEDULE_PENDING_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        schedulePendingTimeouts.put(jobId, future);
    }




    public void cancelSchedulePendingTimeout(Long jobId) {
        Optional.ofNullable(schedulePendingTimeouts.remove(jobId)).ifPresent(f -> f.cancel(false));
        schedulePendingStaff.remove(jobId);
        log.warn("[SCHED_PENDING_CANCEL_TIMER] jobId={}", jobId);
    }

    private void publishChange(Long jobId, HEADJobState prev, Instant at, String actorUuid) {
        appEvents.publishEvent(
                new HEADJobStateChangedEvent(
                        jobId,
                        prev,
                        at,
                        actorUuid != null ? actorUuid : "SYSTEM"
                )
        );
    }

}
