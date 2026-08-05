package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.listener;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADDateFormats;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobStateChangedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADJobPushNotificationListener {

    private static final Locale LOCALE_MX = Locale.forLanguageTag("es-MX");
    private static final ZoneId ZONE_MX = ZoneId.of("America/Mexico_City");

    private final HEADJobRepository jobRepo;
    private final HEADNotificationSender notificationSender;
    private final HEADOccupationPersonalUserRepository occProfileRepo;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onJobStateChanged(HEADJobStateChangedEvent event) {
        var jobOpt = jobRepo.findById(event.jobId());
        if (jobOpt.isEmpty()) {
            log.warn("[JOB-PUSH] job not found id={}", event.jobId());
            return;
        }

        var job = jobOpt.get();
        var state = job.getState();

        log.info("[JOB-PUSH] jobId={} prev={} new={} actor={}",
                job.getId(), event.prev(), state, event.actorUuid());

        notifyClientIfNeeded(job, state, event.actorUuid());
        notifyStaffIfNeeded(job, state, event.actorUuid());
    }

    private void notifyClientIfNeeded(HEADJob job, HEADJobState state, String actorUuid) {
        var client = job.getClient();
        if (client == null || client.getUuIdUser() == null || client.getUuIdUser().isBlank()) {
            return;
        }

        // Evita push redundante al mismo cliente si él mismo canceló
        if (state == HEADJobState.CANCELLED && client.getUuIdUser().equals(actorUuid)) {
            return;
        }

        String template = resolveClientTemplate(job, state);
        if (template == null) return;

        notificationSender.send(new HEADNotificationCommand(
                client.getUuIdUser(),
                HEADNotificationType.JOB_STATE_UPDATE,
                template,
                buildParams(job, state),
                LOCALE_MX
        ));
    }

    private void notifyStaffIfNeeded(HEADJob job, HEADJobState state, String actorUuid) {
        if (job.getStaffUuid() == null || job.getStaffUuid().isBlank()) {
            return;
        }

        // Evita push redundante al mismo staff si él mismo canceló
        if (state == HEADJobState.CANCELLED && job.getStaffUuid().equals(actorUuid)) {
            return;
        }

        String template = resolveStaffTemplate(job, state);
        if (template == null) return;

        notificationSender.send(new HEADNotificationCommand(
                job.getStaffUuid(),
                HEADNotificationType.JOB_STATE_UPDATE,
                template,
                buildParams(job, state),
                LOCALE_MX
        ));
    }

    private String resolveClientTemplate(HEADJob job, HEADJobState state) {
        if (state == null) return null;

        return switch (state) {
            case SCHEDULED -> job.getServiceMode() == HEADServiceMode.VIDEO
                    ? HEADNotificationTemplates.JOB_SCHEDULED_VIDEO_CLIENT
                    : HEADNotificationTemplates.JOB_SCHEDULED_HOME_CLIENT;

            case EN_ROUTE -> HEADNotificationTemplates.JOB_EN_ROUTE_CLIENT;

            case ARRIVED -> HEADNotificationTemplates.JOB_ARRIVED_CLIENT;

            case COMPLETED -> HEADNotificationTemplates.JOB_COMPLETED_CLIENT;

            case READY -> job.getServiceMode() == HEADServiceMode.VIDEO
                    ? HEADNotificationTemplates.JOB_VIDEO_READY_CLIENT
                    : null;

            case PENDING_ASSIGNMENT -> HEADNotificationTemplates.JOB_PENDING_ASSIGNMENT_CLIENT;

            case UNASSIGNABLE -> HEADNotificationTemplates.JOB_UNASSIGNABLE_CLIENT;

            case SCHEDULE_PENDING -> HEADNotificationTemplates.JOB_SCHEDULE_PENDING_CLIENT;

            case CANCELLED -> hasCancelReason(job)
                    ? HEADNotificationTemplates.JOB_CANCELLED_REASON_CLIENT
                    : HEADNotificationTemplates.JOB_CANCELLED_CLIENT;

            default -> null;
        };
    }

    private String resolveStaffTemplate(HEADJob job, HEADJobState state) {
        if (state == null) return null;

        return switch (state) {
            case SCHEDULED -> job.getServiceMode() == HEADServiceMode.VIDEO
                    ? HEADNotificationTemplates.JOB_SCHEDULED_VIDEO_STAFF
                    : HEADNotificationTemplates.JOB_SCHEDULED_HOME_STAFF;

            case READY -> job.getServiceMode() == HEADServiceMode.VIDEO
                    ? HEADNotificationTemplates.JOB_VIDEO_READY_STAFF
                    : null;

            case COMPLETED -> HEADNotificationTemplates.JOB_COMPLETED_STAFF;

            case CANCELLED -> hasCancelReason(job)
                    ? HEADNotificationTemplates.JOB_CANCELLED_REASON_STAFF
                    : HEADNotificationTemplates.JOB_CANCELLED_STAFF;

            default -> null;
        };
    }

    private Map<String, Object> buildParams(HEADJob job, HEADJobState state) {
        Map<String, Object> params = new HashMap<>();

        params.put("jobId", job.getId());
        params.put("jobState", state != null ? state.name() : "");
        params.put("professionalName", resolveProfessionalName(job));
        params.put("professionalLabel", resolveProfessionalLabel(job));
        params.put("cancelReason", resolveCancelReason(job));

        if (job.getScheduledTime() != null) {
            params.put("scheduledTime", HEADDateFormats.formatTime(
                    job.getScheduledTime(),
                    ZONE_MX,
                    LOCALE_MX
            ));
        } else {
            params.put("scheduledTime", "");
        }

        return params;
    }

    private String resolveProfessionalName(HEADJob job) {
        if (job.getStaffUser() == null) return "tu profesional";

        var occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(job.getStaffUser().getIdUser());
        return HEADNameFormatters.buildStaffDisplayName(job.getStaffUser(), occCode);
    }

    private String resolveProfessionalLabel(HEADJob job) {
        if (job.getStaffUser() == null) return "profesional";

        return occProfileRepo.findOccupationLabelsByStaffUserId(job.getStaffUser().getIdUser())
                .stream()
                .findFirst()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .orElse("profesional");
    }

    private boolean hasCancelReason(HEADJob job) {
        return job.getCancelReason() != null;
    }

    private String resolveCancelReason(HEADJob job) {
        HEADCancelReason reason = job.getCancelReason();
        if (reason == null) return "";

        return switch (reason) {
            case SEARCH_TIMEOUT -> "no encontramos personal disponible a tiempo";
            case REASSIGNED -> "el servicio fue reasignado";
            default -> "hubo un cambio en la atención";
        };
    }
}