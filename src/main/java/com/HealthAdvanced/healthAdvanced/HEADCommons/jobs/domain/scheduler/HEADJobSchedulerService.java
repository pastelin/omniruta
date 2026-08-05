package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.scheduler;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADDateFormats;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobStateChangedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADJobSchedulerService {

    private final HEADJobRepository jobRepo;
    private final ApplicationEventPublisher publisher;

    // Para push directo (recordatorio)
    private final HEADNotificationSender notificationSender;

    // (opcional) marcar ocupado en la hora
    private final HEADStaffStateStore staffStateStore;
    private final HEADOccupationPersonalUserRepository occProfileRepo;

    // config
    private static final int REMINDER_MINUTES = 30;
    private static final long ACTIVATE_SKEW_SECONDS = 5;

    @Scheduled(fixedDelayString = "${head.scheduler.jobs.fixedDelayMs:60000}")
    @Transactional
    public void processScheduledJobs() {

        Instant now = Instant.now();

        // 1) Reminder 30 min antes
        sendScheduleReminders(now);

        // 2) Activación a la hora
        activateDueJobs(now);
    }

    private void sendScheduleReminders(Instant now) {

        Instant from = now;
        Instant to = now.plusSeconds(REMINDER_MINUTES * 60L);

        List<HEADJob> jobs = jobRepo
                .findTop200ByStateAndScheduledTimeBetweenAndScheduleReminderSentFalseOrderByScheduledTimeAsc(
                        HEADJobState.SCHEDULED, from, to
                );

        if (jobs.isEmpty()) return;

        jobs.forEach(job -> {
            try {
                // Marca primero para evitar duplicados si se vuelve a correr
                job.setScheduleReminderSent(true);
                job.setScheduleReminderSentAt(now);
                jobRepo.save(job);

                // Push al CLIENTE (y puedes mandar al staff también)
                sendReminderPush(job);

                log.info("[JOB-SCHED] reminder sent jobId={} scheduledTime={} mode={}",
                        job.getId(), job.getScheduledTime(), job.getServiceMode());

            } catch (Exception ex) {
                log.error("[JOB-SCHED] reminder failed jobId={} err={}", job.getId(), ex.toString(), ex);
            }
        });
    }

    private void sendReminderPush(HEADJob job) {
        if (job.getClient() == null || job.getClient().getUuIdUser() == null || job.getClient().getUuIdUser().isBlank()) {
            return;
        }

        Locale locale = Locale.forLanguageTag("es-MX");
        ZoneId zone = ZoneId.of("America/Mexico_City");

        String clientTemplate = (job.getServiceMode() == HEADServiceMode.VIDEO)
                ? HEADNotificationTemplates.JOB_REMINDER_VIDEO_CLIENT
                : HEADNotificationTemplates.JOB_REMINDER_HOME_CLIENT;

        String staffTemplate = (job.getServiceMode() == HEADServiceMode.VIDEO)
                ? HEADNotificationTemplates.JOB_REMINDER_VIDEO_STAFF
                : HEADNotificationTemplates.JOB_REMINDER_HOME_STAFF;

        String scheduledTime = job.getScheduledTime() != null
                ? HEADDateFormats.formatTime(job.getScheduledTime(), zone, locale)
                : "";

        var params = new HashMap<String, Object>();
        params.put("jobId", job.getId());
        params.put("professionalName", resolveProfessionalName(job));
        params.put("professionalLabel", resolveProfessionalLabel(job));
        params.put("scheduledTime", scheduledTime);

        params.put("collapseKey", "APT_REMINDER_" + job.getId());
        params.put("tag", "APT_REMINDER_" + job.getId());
        params.put("ttlSeconds", 3600);
        params.put("androidPriority", "HIGH");
        params.put("channelId", "job_updates");
        params.put("deeplink", "head://job/" + job.getId());

        notificationSender.send(new HEADNotificationCommand(
                job.getClient().getUuIdUser(),
                HEADNotificationType.APPOINTMENT_REMINDER,
                clientTemplate,
                params,
                locale
        ));

        if (job.getStaffUuid() != null && !job.getStaffUuid().isBlank()) {
            var staffParams = new HashMap<String, Object>(params);
            staffParams.put("collapseKey", "APT_REMINDER_STAFF_" + job.getId());
            staffParams.put("tag", "APT_REMINDER_STAFF_" + job.getId());
            staffParams.put("deeplink", "head://staff/job/" + job.getId());

            notificationSender.send(new HEADNotificationCommand(
                    job.getStaffUuid(),
                    HEADNotificationType.APPOINTMENT_REMINDER,
                    staffTemplate,
                    staffParams,
                    locale
            ));
        }
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

    private void activateDueJobsOld(Instant now) {

        List<HEADJob> due = jobRepo
                .findTop200ByStateAndScheduledTimeLessThanEqualOrderByScheduledTimeAsc(
                        HEADJobState.SCHEDULED, now
                );

        if (due.isEmpty()) return;

        due.forEach(job -> {
            try {
                activateOne(job, now);
            } catch (Exception ex) {
                log.error("[JOB-SCHED] activate failed jobId={} err={}", job.getId(), ex.toString(), ex);
            }
        });
    }

    private void activateDueJobs(Instant now) {
        Instant cutoff = now.minusSeconds(ACTIVATE_SKEW_SECONDS);

        List<HEADJob> due = jobRepo
                .findTop200ByStateAndScheduledTimeLessThanEqualOrderByScheduledTimeAsc(
                        HEADJobState.SCHEDULED, cutoff
                );

        if (due.isEmpty()) return;

        due.forEach(job -> {
            try { activateOne(job, now); }
            catch (Exception ex) { log.error("[JOB-SCHED] activate failed jobId={} err={}", job.getId(), ex.toString(), ex);; }
        });
    }

    private void activateOne(HEADJob job, Instant now) {
        if (job.getScheduledTime() == null) return;

        HEADJobState prev = job.getState();

        HEADJobState next = (job.getServiceMode() == HEADServiceMode.HOME)
                ? HEADJobState.EN_ROUTE
                : HEADJobState.READY;

        if (prev == next) return;

        int updated = jobRepo.advanceState(job.getId(), HEADJobState.SCHEDULED, next, now);
        if (updated == 0) return; // ya lo movió alguien o ya no está SCHEDULED

        if (job.getStaffUuid() != null && !job.getStaffUuid().isBlank()) {
            staffStateStore.setBusy(job.getStaffUuid(), true, job.getId());
        }

        publisher.publishEvent(new HEADJobStateChangedEvent(
                job.getId(), prev, now, "SYSTEM"
        ));

        log.info("[JOB-SCHED] activated jobId={} {} -> {} mode={}",
                job.getId(), prev, next, job.getServiceMode());
    }

}

