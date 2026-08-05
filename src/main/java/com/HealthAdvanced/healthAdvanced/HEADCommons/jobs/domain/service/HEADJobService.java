package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;




import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADJobArrivalPinDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffScheduleProposeMultiRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service.HEADRoutingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADDateFormats;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobQueryService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.machine.HEADJobStateMachine;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.DoctorAvailabilityRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADAvailabilityRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADClientScheduleSelectRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADStaffScheduleProposeRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.response.HEADErrorAckEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.*;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.adjustment.HEADCalculateJobFinancialService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.enums.HEADReviewState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.redis.HEADPrescriptionDraftRedisStore;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.dto.HEADScheduleProposalCache;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.request.HEADStaffProposeScheduleRequest;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.service.HEADScheduleSlotService;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.storeRedis.HEADScheduleProposalStore;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService.HEADPaymentStripeService;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.*;
import java.util.*;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.JOB_SCHEDULE_CONFIRMED;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.ROUTE_TO_CLIENT;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.*;
import static com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates.JOB_SCHEDULED_HOME_CLIENT;
import static org.apache.commons.codec.digest.DigestUtils.sha256;

import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADJobService {

    // --- deps core ---
    private final HEADJobRepository repo;
    private final HEADPersonalUserRepository staffRepo;
    private final HEADStaffStateStore state;
    private final HEADPresenceStore presence;
    private final HEADJwtGenerator jwt;
    private final Clock clock;
    private final ApplicationEventPublisher appEvents;
    private final HEADActiveLocationMapService headActiveLocationMapService;
    private final HEADRoutingService routing;
    private final HEADWsEmitter emitter;
    private final HEADJobQueryService headJobQueryService;
    private final HEADClientsRepository clientsRepository;
    private final HEADNotificationSender notificationSender;
    private final HEADOccupationPersonalUserRepository occProfileRepo;
    private final HEADScheduleSlotService slotService;
    private final HEADScheduleProposalStore scheduleProposalStore;
    private final HEADAcceptDecisionStore acceptDecisionStore;
    private final HEADJobEventPublisher wsEvents;
    private final HEADPaymentStripeService paymentStripeService;
    private final HEADPrescriptionDraftRedisStore draftStore;
    private final HEADCalculateJobFinancialService headCalculateJobFinancialService;

    // --- state machine ---
    private final HEADJobStateMachine sm = new HEADJobStateMachine();
    private static final Duration MIN_ROUTE_INTERVAL = Duration.ofSeconds(30);

    // -------------------------
    // Helpers
    // -------------------------

    private Instant now() {
        return Instant.now(clock);
    }

    private HEADJob jobForUpdate(Long jobId) {
        return repo.findByIdForUpdate(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + jobId));
    }

    private HEADPersonalUser currentStaffOrThrow() {
        String uuid = jwt.getUserNamePersonalUser();
        if (uuid == null || uuid.isBlank()) {
            throw new HEADBadRequestException("No staff in token");
        }
        return staffRepo.findByUidUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff not found for UUID: " + uuid));
    }

    private void publishChange(HEADJob saved, HEADJobState prev, Instant at, String actorUuid) {
        appEvents.publishEvent(
                new HEADJobStateChangedEvent(
                        saved.getId(),
                        prev,
                        at,
                        actorUuid != null ? actorUuid : "SYSTEM"
                )
        );
    }

    private HEADPersonalUser currentStaff(String uuIdUser) {
        return staffRepo.findByUidUser(uuIdUser)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found for UUID: " + uuIdUser));
    }

    // -------------------------
    // Availability / snapshot
    // -------------------------

    public HEADStaffStateDto setAvailability(HEADAvailabilityRequest req) {
        String uuid = jwt.getUserNamePersonalUser();
        return state.setAvailability(uuid, req.online());
    }

    public HEADStaffSnapshot getState() {
        String uuid = jwt.getUserNamePersonalUser();
        return new HEADStaffSnapshot(state.get(uuid), presence.isOnline(uuid));
    }

    // -------------------------
    // Generic transitions
    // -------------------------

    /**
     * Transición genérica para sistema.
     * ARRIVED / STARTED / COMPLETED / UNASSIGNABLE / CANCELLED(system) etc.
     * NO usar para ACCEPT/REJECT, hay métodos dedicados.
     */
    @Transactional
    public HEADJob transition(Long jobId, HEADJobState next) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        sm.apply(job, next, at);

        var saved = repo.saveAndFlush(job);
        publishChange(saved, prev, at, "SYSTEM");
        return saved;
    }

    /**
     * Transición por staff logueado (ARRIVED/STARTED/COMPLETED).
     * Asegura pertenencia del job al staff.
     */
    @Transactional
    public HEADJob transitionByStaff(Long jobId, HEADJobState next) {
        var staff = currentStaffOrThrow();
        var job   = jobForUpdate(jobId);

        if (job.getStaffUser() == null || !job.getStaffUser().equals(staff)) {
            throw new HEADBadRequestException("Staff not assigned to this job");
        }

        var prev = job.getState();
        var at   = now();

        sm.apply(job, next, at);

        var saved = repo.saveAndFlush(job);
        publishChange(saved, prev, at, staff.getUidUser());
        return saved;
    }

    // -------------------------
    // Offer (called by RideAssignmentService)
    // -------------------------

    /**
     * Pasa job a OFFERED y marca hasServiceRequest.
     * IMPORTANTE: setea offerExpiresAt aquí para evitar nulls en DTO.
     */
    @Transactional
    public HEADJob offerJobToStaff(Long jobId, HEADPersonalUser staff) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        sm.offer(job, staff, at);

        job.setOfferExpiresAt(at.plusSeconds(HEADRideAssignmentService.OFFER_TIMEOUT_SECONDS));
        var staffActive = state.get(staff.getUidUser());
        var locationAndDistance = headActiveLocationMapService.distanceKmCeilStep(job.getClientLat(), job.getClientLng(), staffActive.lat(), staffActive.lng(), 0.5);
        var distanceMetersInt = headActiveLocationMapService.calculateDistanceInMetersSafeInt(job.getClientLat(),job.getClientLng(), staffActive.lat(), staffActive.lng());
        var durationInSeconds = headActiveLocationMapService.calculateTravelSeconds(distanceMetersInt);
        var durationMinutes = headActiveLocationMapService.calculateTravelMetersTimeInMinutes(distanceMetersInt);
        job.setDurationSeconds(durationInSeconds);
        job.setDurationMinBucket(durationMinutes);
        job.setDistanceMeters(distanceMetersInt);
        job.setDistanceKmBucket(locationAndDistance);
        var saved = repo.saveAndFlush(job);

        // memoria: staff con solicitud pendiente
        state.hasServiceRequest(staff.getUidUser(), jobId);

        // evento liviano (WS AFTER_COMMIT)
        publishChange(saved, prev, at, staff.getUidUser());
        return saved;
    }

    // -------------------------
    // Accept / Reject (Uber flow)
    // -------------------------

    @Transactional
    public HEADJob acceptByStaff(String staffUuid, Long jobId) {

        var staff = staffRepo.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff not found for UUID: " + staffUuid));

        Instant at = now();

        int updated = repo.acceptIfStillOfferedByStaffId(
                jobId,
                staff.getIdUser(),
                at,
                HEADJobState.ACCEPTED_AWAITING_START,
                HEADJobState.ACCEPTED
        );

        log.info("ACCEPT UPDATE COUNT jobId={} staffId={} updated={}", jobId, staff.getIdUser(), updated);

        if (updated == 0) {
            throw new HEADBadRequestException("La oferta ya expiró o no es válida.");
        }

        // recarga el job ya aceptado
        HEADJob saved = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + jobId));

        // memoria
        state.assignJob(staff.getUidUser(), jobId);
        appEvents.publishEvent(new HEADJobAcceptedEvent(saved.getId(), staffUuid));
        // evento dominio
        //publishChange(saved, HEADJobState.ACCEPTED_AWAITING_START, at, staff.getUidUser());

        return saved;
    }

    @Transactional
    public HEADJob acceptAndAwaitStartDecision(Long jobId, String staffUuid) {
        var staff = staffRepo.findByUidUser(staffUuid).orElseThrow(() -> new HEADBadRequestException("Staff not found for UUID: " + staffUuid));;
        var at = now();

        // 1) Accept (tu update atómico)
        int updated = repo.acceptIfStillOfferedByStaffId(
                jobId, staff.getIdUser(), at,
                HEADJobState.OFFERED, HEADJobState.ACCEPTED_AWAITING_START
        );
        if (updated == 0) throw new HEADBadRequestException("La oferta ya expiró o no es válida.");

        var saved = repo.findById(jobId).orElseThrow();

        saved.setOfferExpiresAt(at.plusSeconds(HEADRideAssignmentService.OFFER_TIMEOUT_SECONDS));
        repo.save(saved);
        // 2) Guardar “ventana de decisión” (TTL)
        acceptDecisionStore.save(new HEADAcceptDecisionCache(
                jobId,
                staff.getIdUser(),
                "America/Mexico_City",
                at
        ));

        // 3) memoria + eventos
        state.assignJob(staff.getUidUser(), jobId);
        publishChange(saved, HEADJobState.OFFERED, at, staff.getUidUser());

        return saved;
    }





    @Transactional
    public HEADJob accept(Long jobId) {
        var staff = currentStaffOrThrow();
        return acceptByStaff(staff.getUidUser(),jobId);
    }

    @Transactional
    public HEADJob rejectByStaff(Long jobId, String note) {
        currentStaffOrThrow(); // valida token
        return reject(jobId, note);
    }

    @Transactional
    public HEADJob reject(Long jobId, String note) {
        var staff = currentStaffOrThrow();
        var job   = jobForUpdate(jobId);

        if (job.getStaffUser() == null || !job.getStaffUser().equals(staff)) {
            throw new HEADBadRequestException("Staff is not authorized for this job offer.");
        }

        var prev = job.getState();
        var at   = now();

        sm.reject(job, at, note);

        var saved = repo.saveAndFlush(job);

        // quitar bandera de solicitud / sumar rechazo
        state.rejectedService(staff.getUidUser(), jobId);
        state.releaseOffer(staff.getUidUser());

        publishChange(saved, prev, at, staff.getUidUser());
        return saved;
    }

    // -------------------------
    // Expire (called by scheduler)
    // -------------------------

    @Transactional
    public HEADJob expire(Long jobId) {

        Instant at = now(); // Instant.now(clock)

        int updated = repo.expireIfStillOffered(
                jobId,
                at,
                HEADJobState.OFFERED,
                HEADJobState.EXPIRED,
                HEADCancelledBy.SYSTEM,
                HEADCancelReason.REASSIGNED // o el enum que uses para expiración
        );

        // Si no actualizó, es porque:
        //  - ya fue ACCEPTED/REJECTED/CANCELLED
        //  - o todavía no vence
        if (updated == 0) {
            return repo.findById(jobId).orElse(null);
        }

        // Recarga el job expirada (clearAutomatically evita entidad “vieja”)
        var saved = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found: " + jobId));

        // libera banderas del staff
        var staff = saved.getStaffUser();
        if (staff != null) {
            state.releaseOffer(staff.getUidUser());
        }

        publishChange(saved, HEADJobState.OFFERED, at, "SYSTEM");
        return saved;
    }



    // -------------------------
    // Cancel / Withdraw (client/system)
    // -------------------------

    @Transactional
    public void cancelByClient(String uuIdClient, Long jobId, HEADCancelReason reason, String note) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        var clientCurrentSocket = clientsRepository.findByUuIdUser(uuIdClient)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        paymentStripeService.cancelPaymentForJob(job, reason, clientCurrentSocket);

        sm.cancelByClient(job, at, reason, note);

        var saved = repo.saveAndFlush(job);

        Optional.ofNullable(saved.getStaffUser())
                .map(HEADPersonalUser::getUidUser)
                .ifPresent(state::releaseOffer);

        state.clearJob(job.getStaffUuid());

        String actor = Optional.ofNullable(saved.getClient())
                .map(HEADClients::getUuIdUser)
                .orElse("CLIENT");

        publishChange(saved, prev, at, actor);
    }


    @Transactional
    public HEADJob cancelBySystem(Long jobId, HEADCancelReason reason, String note) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        paymentStripeService.cancelPaymentForJobBySystem(job, reason); // nuevo método

        sm.cancelBySystem(job, at, reason, note);

        var saved = repo.saveAndFlush(job);

        Optional.ofNullable(saved.getStaffUser())
                .map(HEADPersonalUser::getUidUser)
                .ifPresent(state::releaseOffer);

        String actor = Optional.ofNullable(saved.getClient())
                .map(HEADClients::getUuIdUser)
                .orElse("SYSTEM");

        publishChange(saved, prev, at, actor);
        return saved;
    }

    @Transactional
    public HEADJob cancelByStaff(String uuIdStaff, Long jobId, HEADCancelReason reason, String note) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        var clientCurrentSocket = staffRepo.findByUidUser(uuIdStaff)
                .orElseThrow(() -> new HEADBadRequestException("Personal no encontrado"));

        paymentStripeService.cancelPaymentForJobBySystem(job, reason); // nuevo método

        sm.cancelByStaff(job, at, reason, note);

        var saved = repo.saveAndFlush(job);

        Optional.ofNullable(saved.getStaffUser())
                .map(HEADPersonalUser::getUidUser)
                .ifPresent(state::releaseOffer);

        state.clearJob(job.getStaffUuid());

        String actor = Optional.ofNullable(saved.getStaffUser())
                .map(HEADPersonalUser::getUidUser)
                .orElse("STAFF");

        publishChange(saved, prev, at, actor);
        return saved;
    }

    /**
     * Retiro por sistema (ej. cliente se desconectó, etc.)
     */
    @Transactional
    public HEADJob withdraw(Long jobId, String note) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        sm.withdraw(job, at, note);

        var saved = repo.saveAndFlush(job);

        Optional.ofNullable(saved.getStaffUser())
                .map(HEADPersonalUser::getUidUser)
                .ifPresent(state::releaseOffer);

        publishChange(saved, prev, at, "SYSTEM");
        return saved;
    }

    // -------------------------
    // Payment helpers (si los usas)
    // -------------------------

    @Transactional
    public HEADJob markPaymentCaptured(Long jobId, String paymentId) {
        var job  = jobForUpdate(jobId);
        var prev = job.getState();
        var at   = now();

        job.setPaymentStatus(HEADPaymentStatus.CAPTURED);
        job.setPaymentId(paymentId);
        job.setCapturedAt(at);

        var saved = repo.saveAndFlush(job);
        publishChange(saved, prev, at, "SYSTEM");
        return saved;
    }

    @Transactional
    public HEADJob findById(Long jobId) {
        return repo.findById(jobId).orElse(null);
    }

    @Transactional
    public void refreshRouteIfNeeded(String uuIdUser, double staffLat, double staffLng) {
        var jobId = findActiveJobByStaff(uuIdUser).orElse(null);
        if (jobId == null) {
            //currentForStaff(uuIdUser);
            return;
        }
        var job = repo.findById(jobId.getId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + jobId));

        // solo tiene sentido si ya hay cliente con coords
        if (job.getClientLat() == null || job.getClientLng() == null) {
            log.warn("[ROUTE] job={} no tiene clientLat/Lng", jobId);
            return;
        }

        if (job.getState() != HEADJobState.EN_ROUTE) {
            return;
        }

        var now = Instant.now();

        // 1) throttle para no spamear Google
        if (tooSoonSinceLastRoute(job, now)) {
            log.debug("[ROUTE] skip job={} (tooSoonSinceLastRoute)", jobId);
            return;
        }

        // (opcional) 2) validar que el staff se haya movido suficiente
        // if (!movedEnough(job, staffLat, staffLng)) return;

        // 3) llamar Directions
        var route = routing.routeStaffToClient(
                staffLat, staffLng,
                job.getClientLat(), job.getClientLng()
        );

        // 4) persistir métricas + bounds
        job.setDistanceMeters(route.distanceMeters());
        job.setDurationSeconds(route.durationSeconds());
        job.setStartAddress(route.startAddress());
        job.setEndAddress(route.endAddress());
        job.setRouteNorthLat(route.northLat());
        job.setRouteEastLng(route.eastLng());
        job.setRouteSouthLat(route.southLat());
        job.setRouteWestLng(route.westLng());
        // si agregas lastRouteAt:
        // job.setLastRouteAt(now);

        repo.saveAndFlush(job);
        // 5) emitir al staff la nueva ruta
        emitter.toUser(job.getStaffUuid(), ROUTE_TO_CLIENT, route);
        var clientUuid = job.getClient().getUuIdUser();
        if (clientUuid != null && !clientUuid.isBlank()) {
            emitter.emitToClient(clientUuid, ROUTE_TO_CLIENT, route);
        }
        publishChange(job,job.getState(),Instant.now(),job.getStaffUuid());
    }


    public Optional<HEADJob> findActiveJobByStaff(String staffUuid) {
        var activeStates = List.of(
                HEADJobState.EN_ROUTE
        );

        return repo.findFirstByStaffUuidAndStateInOrderByUpdatedAtDesc(
                staffUuid,
                activeStates
        );
    }

    private boolean tooSoonSinceLastRoute(HEADJob job, Instant now) {
        Instant last = job.getUpdatedAt(); // o lastRouteAt si agregas el campo
        if (last == null) return false;
        return Duration.between(last, now).compareTo(MIN_ROUTE_INTERVAL) < 0;
    }

    @Transactional
    public void markArrived(Long jobId, String staffUuid) {

        var job = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + jobId));

        if (!Objects.equals(job.getStaffUuid(), staffUuid)) {
            throw new HEADBadRequestException("Staff no asignado a este job");
        }

        if (job.getState() != HEADJobState.EN_ROUTE) {
            throw new HEADBadRequestException("Solo se puede marcar ARRIVED desde EN_ROUTE");
        }

        var now = Instant.now(clock);

        String pin = HEADCommonsUtils.generatePin4();
        job.setArrivalPinHash(HEADCommonsUtils.sha256Hex(pin));
        job.setArrivalPinCreatedAt(now);
        job.setArrivalPinAttempts(0);

        job.setState(HEADJobState.ARRIVED);
        job.setArrivedAt(now);
        repo.save(job);

        wsEvents.jobArrivalPinIssued(job.getClient().getUuIdUser(), jobId, pin);

        publishChange(job, HEADJobState.EN_ROUTE, now, staffUuid);
    }

    @Transactional
    public void markStarted(Long jobId, String staffUuid) {

        var job = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + jobId));

        if (!Objects.equals(job.getStaffUuid(), staffUuid)) {
            throw new HEADBadRequestException("Staff no asignado a este job");
        }

        if (job.getServiceMode() != HEADServiceMode.VIDEO) {
            throw new HEADBadRequestException("Para iniciar este servicio se requiere PIN");
        }

        var now = Instant.now(clock);
        job.setState(HEADJobState.READY);
        job.setStartedAt(now);
        repo.save(job);

        publishChange(job, HEADJobState.ACCEPTED_AWAITING_START, now, staffUuid);
    }

    @Transactional
    public void markStartedWithPin(Long jobId, String staffUuid, String pin) {

        var job = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + jobId));

        if (!Objects.equals(job.getStaffUuid(), staffUuid)) {
            throw new HEADBadRequestException("Staff no asignado a este job");
        }

        if (job.getServiceMode() == HEADServiceMode.VIDEO) {
            throw new HEADBadRequestException("Para VIDEO usa JOB_STARTED");
        }


        if (job.getState() != HEADJobState.ARRIVED) {
            throw new HEADBadRequestException("Solo se puede marcar STARTED desde ARRIVED");
        }

        // ---- validar PIN ----
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new HEADBadRequestException("PIN inválido");
        }

        String expectedHash = job.getArrivalPinHash();
        if (expectedHash == null || expectedHash.isBlank()) {
            throw new HEADBadRequestException("No existe PIN para este servicio");
        }

        int attempts = job.getArrivalPinAttempts() == null ? 0 : job.getArrivalPinAttempts();
        if (attempts >= 5) {
            throw new HEADBadRequestException("Demasiados intentos. Solicita un nuevo PIN.");
        }

        String actualHash = HEADCommonsUtils.sha256Hex(pin.trim());
        if (!java.util.Objects.equals(actualHash, expectedHash)) {
            job.setArrivalPinAttempts(attempts + 1);
            repo.save(job);
            throw new HEADBadRequestException("PIN incorrecto");
        }

        var now = Instant.now(clock);

        // opcional: registrar que ya validó PIN (auditoría)
        job.setArrivalPinVerifiedAt(now);
        job.setArrivalPinAttempts(0);
        repo.save(job);

        // ---- si cobró OK, ahora sí STARTED ----
        job.setState(HEADJobState.STARTED);
        job.setStartedAt(now);
        repo.save(job);

        publishChange(job, HEADJobState.ARRIVED, now, staffUuid);
    }




    @Transactional
    public void markCompletedByStaff(long jobId, Boolean isIssue, String staffUuid) {
        var job = repo.findById(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + jobId));

        var occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(job.getStaffUser().getIdUser());
        if (occCode == HEADOccupationCode.DOCTOR) {
            if (!isIssue) {
                throw new HEADBadRequestException("La receta aun no se ha emitido");
            }
            draftStore.delete(jobId);
        }

        if (job.getState() != HEADJobState.STARTED && job.getState() != HEADJobState.READY) {
            throw new HEADBadRequestException("Job not in STARTED");
        }
        var jobBeforeState = job.getState();
        paymentStripeService.capturePaymentForJob(jobId);
        job.setState(HEADJobState.COMPLETED);
        job.setCompletedAt(clock.instant());
        job.setReviewState(HEADReviewState.PENDING);

        if (job.getStartedAt() != null) {
            long seconds = Duration.between(job.getStartedAt(), job.getCompletedAt()).toSeconds();
            job.setDurationSeconds(seconds);
        }

        repo.save(job);
        headCalculateJobFinancialService.createSnapshotForCompletedJob(jobId, HEADPaymentProcessor.STRIPE);
        state.markJobFinished(staffUuid);
        publishChange(job, jobBeforeState, job.getCompletedAt(), staffUuid);
    }

    @Transactional
    public HEADJob currentForStaff(String staffUuid) {

        // 1) buscar staffUser por uuid, obtener id
        // (asumo que ya tienes HEADPersonalUserRepository)

        var staff = staffRepo.findByUidUser(staffUuid).orElse(null);
        if (staff == null) return null;

        var jobs = headJobQueryService.findActiveJobCurrentsForStaffUserId(staff.getIdUser());

        var job = jobs.stream().findFirst().orElse(null);
        if (job == null) return null;

        publishChange(job, job.getState(), Instant.now(), staffUuid);

        return job;
    }

    @Transactional
    public HEADJob currentForClient(String clientUuid) {



        var client = clientsRepository.findByUuIdUser(clientUuid).orElse(null);
        if (client == null) return null;

        var jobs = headJobQueryService.findActiveJobCurrentsForClientUserId(client.getIdUser());

        var job = jobs.stream().findFirst().orElse(null);
        if (job == null) return null;

        publishChange(job, job.getState(), Instant.now(), clientUuid);

        if (job.getState() == HEADJobState.ARRIVED) {
            var now = Instant.now(clock);
            String pin = HEADCommonsUtils.generatePin4();
            job.setArrivalPinHash(HEADCommonsUtils.sha256Hex(pin));
            job.setArrivalPinCreatedAt(now);
            job.setArrivalPinAttempts(0);
            repo.save(job);
            emitter.emitToClient(clientUuid,JOB_ARRIVAL_PIN_ISSUED, new HEADJobArrivalPinDto(job.getId(), pin));
        }

        return job;
    }

    @Transactional
    public HEADErrorAckEvent scheduleOfferJob(String staffUuid, DoctorAvailabilityRequest req) {
        var job = repo.findById(req.jobId()).orElse(null);
        if (job == null) return new HEADErrorAckEvent("Servicio no encontrado", false);

        var clientUuid = job.getClient().getUuIdUser();
        if (clientUuid == null) return new HEADErrorAckEvent("No se encontro el cliente", false);

        // ✅ pasar a pending
        job.setState(HEADJobState.SCHEDULE_PENDING);
        repo.save(job);

        // ✅ cliente ve “verificando”
        emitter.emitToClient(staffUuid, CLIENT_SCHEDULE_PENDING, new HEADClientUpdateDto(
                req.jobId(),
                HEADJobState.SCHEDULE_PENDING.name(),
                "Estamos verificando la disponibilidad del personal…",
                null, null, null, null, null, null, null
        ));

        return new HEADErrorAckEvent(null, true);
    }

    @Transactional
    public HEADErrorAckEvent staffProposeSchedule(String staffUuid, HEADStaffScheduleProposeRequest req) {
        var job = repo.findById(req.jobId()).orElse(null);
        if (job == null) return new HEADErrorAckEvent("Servicio no encontrado", false);

        var clientUuid = job.getClient().getUuIdUser();
        if (clientUuid == null) return new HEADErrorAckEvent("No se encontro el cliente", false);

        // guard: debe estar pending y asignado a ese staff
        if (job.getState() != HEADJobState.SCHEDULE_PENDING)
            return new HEADErrorAckEvent("El servicio ya no está en agendamiento", false);

        if (!Objects.equals(job.getStaffUuid(), staffUuid))
            return new HEADErrorAckEvent("No eres el staff asignado", false);

        // construir slots disponibles
        var slots = slotService.buildSlotsForStaffDay(new HEADStaffProposeScheduleRequest(
                req.jobId(), req.tz(), req.dayOffset(), req.startTime(), req.endTime(), req.stepMin()
        ));

        // emitir al cliente la propuesta (slots)
        emitter.emitToClient(clientUuid, CLIENT_OFFER_SCHEDULE, slots);

        return new HEADErrorAckEvent(null, true);
    }


    @Transactional
    public HEADErrorAckEvent staffProposeScheduleMulti(String staffUuid, HEADStaffScheduleProposeMultiRequest req) {
        var job = repo.findById(req.jobId()).orElse(null);
        if (job == null) return new HEADErrorAckEvent("Servicio no encontrado", false);

        var clientUuid = job.getClient().getUuIdUser();
        if (clientUuid == null) return new HEADErrorAckEvent("No se encontro el cliente", false);

        if (job.getState() != HEADJobState.SCHEDULE_PENDING)
            return new HEADErrorAckEvent("El servicio ya no está en agendamiento", false);

        if (!Objects.equals(job.getStaffUuid(), staffUuid))
            return new HEADErrorAckEvent("No eres el staff asignado", false);

        // Validaciones mínimas
        if (req.selectedStartAts() == null || req.selectedStartAts().isEmpty())
            return new HEADErrorAckEvent("Selecciona al menos un horario", false);

        // Guarda en Redis con TTL
        long now = System.currentTimeMillis();
        var packageCurrent = Optional.ofNullable(job.getRequest())
                .map(HEADServiceRequestClient::getPkg);

        int durationMin = packageCurrent.map(HEADPackagesPersonal::getServiceDurationMin)
                .orElse(60);
        var cache = new HEADScheduleProposalCache(
                req.jobId(),
                staffUuid,
                req.tz(),
                req.dayOffset(),
                req.selectedStartAts().stream().filter(Objects::nonNull).map(dateCurrent -> {
                        return HEADDateFormats.convertStrToInstantTz(dateCurrent).toEpochMilli();
                }).distinct().toList(),
                now,
                now + Duration.ofMinutes(10).toMillis(),
                req.selectedStarEnd(),
                durationMin
        );
        scheduleProposalStore.save(cache);
        emitter.emitToClient(clientUuid, CLIENT_OFFER_SCHEDULE, cache);
        return new HEADErrorAckEvent(null, true);
    }




    @Transactional
    public HEADScheduleConfirmed offerScheduleSelect(String clientUuid, HEADClientScheduleSelectRequest request) {
        var client = clientsRepository.findByUuIdUser(clientUuid).orElse(null);
        if (client == null) return null;

        var jobCurrent = repo.findById(request.jobId()).orElse(null);
        if (jobCurrent == null) return null;

        var staffUser = jobCurrent.getStaffUser();
        if (staffUser == null) return null;

        var prev = jobCurrent.getState();
        var staffUserId = staffUser.getIdUser();

        Instant timeSelect = Instant.ofEpochMilli(request.scheduledTime());
        jobCurrent.setScheduledTime(timeSelect);
        jobCurrent.setState(HEADJobState.SCHEDULED);

        var saved = repo.saveAndFlush(jobCurrent);

        publishChange(saved, prev, now(), clientUuid);

        state.releaseOffer(saved.getStaffUuid());

        var occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(staffUserId);
        var nameStaff = HEADNameFormatters.buildStaffDisplayName(staffUser, occCode);
        var nameOccupation = occProfileRepo.findOccupationLabelsByStaffUserId(staffUserId)
                .stream()
                .findFirst()
                .orElse("N/A");

        ZoneId zone = ZoneId.of("America/Mexico_City");
        String t = HEADDateFormats.formatTime(
                saved.getScheduledTime(),
                zone,
                new Locale("es", "MX")
        );

        var packageCurrent = Optional.ofNullable(saved.getRequest())
                .map(HEADServiceRequestClient::getPkg);

        int durationMin = packageCurrent.map(HEADPackagesPersonal::getServiceDurationMin)
                .orElse(60);

        var headScheduleConfirm = new HEADScheduleConfirmed(
                nameStaff,
                nameOccupation,
                t,
                HEADDateFormats.formatDate(timeSelect),
                HEADDateFormats.buildTimeRange(timeSelect, zone, Duration.ofMinutes(durationMin)),
                HEADDateFormats.formatDurationEs(durationMin),
                packageCurrent.map(HEADPackagesPersonal::getTitle).orElse("N/A"),
                saved.getEndAddress(),
                saved.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString()
        );

        emitter.toUser(saved.getStaffUuid(), JOB_SCHEDULE_CONFIRMED, headScheduleConfirm);
        scheduleProposalStore.delete(request.jobId());

        return headScheduleConfirm;
    }

    public HEADScheduleProposalCache getScheduleCurrent(Long jobId) {
        return scheduleProposalStore.get(jobId);
    }

}
