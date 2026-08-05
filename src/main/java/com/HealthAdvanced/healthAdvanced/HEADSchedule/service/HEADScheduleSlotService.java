package com.HealthAdvanced.healthAdvanced.HEADSchedule.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.dto.TimeInterval;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.request.HEADStaffProposeScheduleRequest;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADDaySlots;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduleSlotsResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADSlot;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HEADScheduleSlotService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final HEADJobRepository jobRepo;
    private final HEADJwtGenerator jwt;

    public HEADScheduleSlotsResponse buildSlotsForStaffDay(HEADStaffProposeScheduleRequest req) {
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        if (req.jobId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobId is required");
        if (req.tz() == null || req.tz().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tz is required");
        if (req.startTime() == null || req.endTime() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime/endTime required");
        if (req.stepMin() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stepMin must be > 0");

        var job = jobRepo.findByIdWithRequestAndPkg(req.jobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        int durationMin = Optional.ofNullable(job.getRequest())
                .map(HEADServiceRequestClient::getPkg)
                .map(HEADPackagesPersonal::getServiceDurationMin)
                .orElse(60);

        String staffUuid = jwt.getUserNamePersonalUser();
        if (job.getStaffUuid() != null && !job.getStaffUuid().equals(staffUuid)) {
            throw new HEADBusinessException("Not your job");
        }

        ZoneId zone;
        try {
            zone = ZoneId.of(req.tz());
        } catch (Exception e) {
            throw new HEADBusinessException("Invalid tz");
        }

        LocalDate date = LocalDate.now(zone).plusDays(req.dayOffset());

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(req.startTime(), HH_MM);
            end   = LocalTime.parse(req.endTime(), HH_MM);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format HH:mm");
        }

        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        int bufferMin = 10;
        boolean endIsLastStart = true;

        // ---------- BUSY JOBS: todo el día ----------
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd   = date.plusDays(1).atStartOfDay(zone).toInstant();

        var states = List.of(
                HEADJobState.SCHEDULED,
                HEADJobState.SCHEDULE_PENDING,
                HEADJobState.ACCEPTED_AWAITING_START,
                HEADJobState.ACCEPTED,
                HEADJobState.EN_ROUTE,
                HEADJobState.ARRIVED,
                HEADJobState.STARTED,
                HEADJobState.PAUSED
        );

        List<HEADJob> busyJobs = jobRepo.findScheduledJobsBetweenWithPkg(
                staffUuid,
                dayStart,
                dayEnd,
                states
        );

        List<TimeInterval> busyIntervals = busyJobs.stream()
                .map(j -> toInterval(j, bufferMin))
                .filter(Objects::nonNull)
                .toList();

        // ---------- SLOTS ----------
        int stepMin = chooseStepMin(durationMin);
        LocalTime lastStart = endIsLastStart ? end : end.minusMinutes(durationMin);

        Instant now = Instant.now();
        boolean isToday = LocalDate.now(zone).equals(date);

        // opcional: evitar reservar "en caliente"
        int leadTimeMin = 0;
        Instant minStartAllowed = isToday ? now.plusSeconds(leadTimeMin * 60L) : Instant.MIN;

        List<HEADSlot> slots = Stream.iterate(start, t -> !t.isAfter(lastStart), t -> t.plusMinutes(stepMin))
                .map(t -> {
                    Instant startAt = ZonedDateTime.of(date, t, zone).toInstant();
                    Instant endAt   = startAt.plusSeconds(durationMin * 60L);

                    // candidato a revisar (duración + buffer)
                    Instant candidateEnd = startAt.plusSeconds((durationMin + bufferMin) * 60L);

                    // ✅ (1) deshabilitar si ya pasó (solo hoy)
                    boolean okTime = !startAt.isBefore(minStartAllowed);

                    // ✅ (2) deshabilitar si se empalma con agendado
                    boolean okBusy = busyIntervals.stream().noneMatch(iv -> iv.overlaps(startAt, candidateEnd));

                    boolean available = okTime && okBusy;

                    String reason = null;
                    if (!available) {
                        if (!okTime) reason = "PAST_TIME";
                        else reason = "BOOKED";
                    }

                    return new HEADSlot(t.format(HH_MM), startAt, endAt, available, reason);
                })
                .toList();

        return new HEADScheduleSlotsResponse(
                req.tz(),
                durationMin,
                List.of(new HEADDaySlots(date, slots))
        );
    }

    private TimeInterval toInterval(HEADJob busyJob, int bufferMin) {
        Instant scheduled = busyJob.getScheduledTime();
        if (scheduled == null) return null;

        int busyDurationMin = Optional.ofNullable(busyJob.getRequest())
                .map(HEADServiceRequestClient::getPkg)
                .map(HEADPackagesPersonal::getServiceDurationMin)
                .orElse(60);

        Instant start = scheduled.minusSeconds(bufferMin * 60L);
        Instant end   = scheduled.plusSeconds((busyDurationMin + bufferMin) * 60L);
        return new TimeInterval(start, end);
    }

    private int chooseStepMin(int durationMin) {
        if (durationMin <= 60) return 15;
        if (durationMin <= 180) return 30;
        if (durationMin <= 24 * 60) return 60;
        return 240;
    }
}
