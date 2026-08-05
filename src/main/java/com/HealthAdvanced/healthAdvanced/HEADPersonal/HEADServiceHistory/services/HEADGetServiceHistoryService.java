package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.dto.response.HEADCompletedServiceResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.dto.response.HEADServiceHistoryResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.enums.HEADCompletedServiceStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.persistence.HEADCompletedServiceRowView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetServiceHistoryService {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX")).withZone(MX_ZONE);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(MX_ZONE);

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADJobRepository jobRepository;

    public HEADServiceHistoryResponse execute() {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        Set<HEADJobState> states = Set.of(
                HEADJobState.COMPLETED,
                HEADJobState.CANCELLED
        );

        var summary = jobRepository.getCompletedServiceHistorySummaryForStaff(
                staff.getIdUser(),
                HEADJobState.COMPLETED
        );

        List<HEADCompletedServiceResponse> services = jobRepository
                .findServiceHistoryForStaff(staff.getIdUser(), states)
                .stream()
                .map(this::toResponse)
                .toList();

        return new HEADServiceHistoryResponse(
                summary != null && summary.getTotalServices() != null
                        ? summary.getTotalServices().intValue()
                        : 0,
                summary != null && summary.getTotalEarned() != null
                        ? summary.getTotalEarned()
                        : BigDecimal.ZERO,
                services
        );
    }

    private HEADCompletedServiceResponse toResponse(HEADCompletedServiceRowView row) {
        boolean isVideoCall = "VIDEO".equalsIgnoreCase(String.valueOf(row.getServiceMode()));

        String location = isVideoCall
                ? "Remoto"
                : extractLocation(row.getAddress());

        String duration = (row.getDurationMinutes() != null ? row.getDurationMinutes() : 0) + " min";

        int amount = row.getAmount() != null ? row.getAmount().intValue() : 0;

        return new HEADCompletedServiceResponse(
                row.getId(),
                row.getPatientName() != null ? row.getPatientName() : "",
                row.getServiceName() != null ? row.getServiceName() : "",
                location,
                row.getCompletedAt() != null ? DATE_FORMAT.format(row.getCompletedAt()) : "",
                row.getCompletedAt() != null ? TIME_FORMAT.format(row.getCompletedAt()) : "",
                duration,
                amount,
                mapStatus(row.getJobState())
        );
    }

    private HEADCompletedServiceStatusResponse mapStatus(String state) {
        return "CANCELLED".equalsIgnoreCase(state)
                ? HEADCompletedServiceStatusResponse.CANCELLED
                : HEADCompletedServiceStatusResponse.COMPLETED;
    }

    private String extractLocation(String address) {
        if (address == null || address.isBlank()) return "";
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim() + ", " + parts[parts.length - 1].trim();
        }
        return address;
    }
}
