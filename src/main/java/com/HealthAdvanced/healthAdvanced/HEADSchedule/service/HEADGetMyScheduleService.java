package com.HealthAdvanced.healthAdvanced.HEADSchedule.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADDayStatsResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADMyScheduleResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduleDayResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduledServiceResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.enums.HEADServiceStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.persistence.HEADScheduledServiceRowView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetMyScheduleService {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX")).withZone(MX_ZONE);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(MX_ZONE);

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADJobRepository jobRepository;

    public HEADMyScheduleResponse execute() {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        LocalDate today = LocalDate.now(MX_ZONE);
        LocalDate tomorrow = today.plusDays(1);

        Instant todayFrom = today.atStartOfDay(MX_ZONE).toInstant();
        Instant todayTo = tomorrow.atStartOfDay(MX_ZONE).toInstant();

        Instant tomorrowFrom = tomorrow.atStartOfDay(MX_ZONE).toInstant();
        Instant tomorrowTo = tomorrow.plusDays(1).atStartOfDay(MX_ZONE).toInstant();

        Set<HEADJobState> states = Set.of(
                HEADJobState.SCHEDULE_PENDING,
                HEADJobState.ACCEPTED,
                HEADJobState.EN_ROUTE,
                HEADJobState.STARTED,
                HEADJobState.SCHEDULED
        );

        List<HEADScheduledServiceResponse> todayServices = jobRepository
                .findScheduledServicesForStaff(staff.getIdUser(), todayFrom, todayTo, states)
                .stream()
                .map(this::toResponse)
                .toList();

        List<HEADScheduledServiceResponse> tomorrowServices = jobRepository
                .findScheduledServicesForStaff(staff.getIdUser(), tomorrowFrom, tomorrowTo, states)
                .stream()
                .map(this::toResponse)
                .toList();

        return new HEADMyScheduleResponse(
                buildDayResponse(today, todayServices),
                buildDayResponse(tomorrow, tomorrowServices)
        );
    }

    private HEADScheduleDayResponse buildDayResponse(LocalDate date, List<HEADScheduledServiceResponse> services) {
        int total = services.size();
        int confirmed = (int) services.stream()
                .filter(s -> s.status() == HEADServiceStatusResponse.CONFIRMED)
                .count();
        int pending = (int) services.stream()
                .filter(s -> s.status() == HEADServiceStatusResponse.PENDING)
                .count();
        int totalMinutes = services.stream()
                .map(HEADScheduledServiceResponse::durationMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return new HEADScheduleDayResponse(
                DATE_FORMAT.format(date.atStartOfDay(MX_ZONE).toInstant()),
                new HEADDayStatsResponse(total, confirmed, pending, totalMinutes),
                services
        );
    }

    HEADScheduledServiceResponse toResponse(HEADScheduledServiceRowView row) {
        boolean isVideoCall = "VIDEO".equalsIgnoreCase(String.valueOf(row.getServiceMode()));
        String fullAddress = row.getAddress() != null ? row.getAddress() : "";

        String location = isVideoCall ? "Remoto" : extractLocation(fullAddress);
        String address = isVideoCall ? "Videollamada" : extractStreet(fullAddress);

        return new HEADScheduledServiceResponse(
                row.getId(),
                row.getWhen() != null ? TIME_FORMAT.format(row.getWhen()) : "",
                row.getWhen() != null ? DATE_FORMAT.format(row.getWhen()) : "",
                row.getPatientName() != null ? row.getPatientName() : "",
                row.getServiceName() != null ? row.getServiceName() : "",
                location,
                address,
                mapStatus(row.getJobState()),
                row.getDurationMinutes() != null ? row.getDurationMinutes() : 0,
                isVideoCall,
                row.getServiceDescription() != null && !row.getServiceDescription().isBlank() ? row.getServiceDescription() : null,
                row.getLat(),
                row.getLng()
        );
    }

    private HEADServiceStatusResponse mapStatus(String jobState) {
        return switch (String.valueOf(jobState)) {
            case "SCHEDULE_PENDING" -> HEADServiceStatusResponse.PENDING;
            default -> HEADServiceStatusResponse.CONFIRMED;
        };
    }

    private String extractLocation(String address) {
        if (address == null || address.isBlank()) return "";
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim() + ", " + parts[parts.length - 1].trim();
        }
        return address;
    }

    private String extractStreet(String address) {
        if (address == null || address.isBlank()) return "";
        String[] parts = address.split(",");
        return parts[0].trim();
    }
}