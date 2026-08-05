package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.UiStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionsResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HEADMyPrescriptionsService {

    private final HEADPrescriptionJpaRepository repo;
    private final HEADJwtGenerator jwt;
    private final HEADPrescriptionProgressService progressService;

    private static final int VALID_DAYS = 30;

    public HEADPrescriptionsResponse list() {
        String clientUuid = jwt.getUserNamePersonalUser();
        Instant now = Instant.now();
        Instant from = now.minus(VALID_DAYS, ChronoUnit.DAYS);

        var prescriptions = repo.findByClientUuidOrderByIssuedAtDesc(clientUuid);
        var ids = prescriptions.stream().map(HEADPrescription::getId).toList();

        Map<Long, Integer> progressMap = progressService.progressPercentByPrescriptionIds(clientUuid, ids);

        var items = prescriptions.stream().map(p -> {
            Integer progress = progressMap.getOrDefault(p.getId(), 0);
            var uiStatus = mapUiStatus(p, from);
            return new HEADPrescriptionsResponse.Item(
                    p.getId(),
                    shorten(p.getDiagnosis(), 60),
                    p.getPrescriptionCode(),
                   uiStatus,
            p.getDoctorName(),
                    p.getDoctorSpecialty(),
                    p.getMedications() != null ? p.getMedications().size() : 0,
                    progress,
                    p.getIssuedAt() != null ? p.getIssuedAt().toString() : null,
                    p.getIssuedAt() != null ? p.getIssuedAt().plus(30, ChronoUnit.DAYS).toString() : null
    );
        }).toList();

        int total = items.size();
        int active = (int) items.stream().filter(i -> i.status() == UiStatus.ACTIVE).count();
        int expired = (int) items.stream().filter(i -> i.status() == UiStatus.EXPIRED).count();
        int completed = (int) items.stream().filter(i -> i.status() == UiStatus.COMPLETED).count();

        var summary = new HEADPrescriptionsResponse.Summary(total, active, completed, expired);
        return new HEADPrescriptionsResponse(summary, items);
    }

    private UiStatus mapUiStatus(HEADPrescription p, Instant activeFrom) {
        if (p.getStatus() == HEADPrescriptionStatus.VOID) return UiStatus.VOID;
        if (p.getIssuedAt() == null) return UiStatus.EXPIRED;
        return p.getIssuedAt().isBefore(activeFrom) ? UiStatus.EXPIRED : UiStatus.ACTIVE;
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}