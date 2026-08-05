package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response.HEADMedicationsTodayResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADDoseTodayRow;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationDoseRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationScheduleRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class HEADMedicationsTodayService {

    private final HEADMedicationDoseRepository doseRepo;
    private final HEADMedicationTrackingService trackingService;
    private final HEADMedicationScheduleRepository scheduleRepo;
    private final HEADJwtGenerator jwt;

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");

    @Transactional
    public HEADMedicationsTodayResponse today() {
        String clientUuid = jwt.getUserNamePersonalUser();
        LocalDate today = LocalDate.now(ZONE);

        // asegura dosis de hoy
        scheduleRepo.findAllByClientUuidAndActiveTrue(clientUuid)
                .forEach(s -> {
                    if (!today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate())) {
                        trackingService.ensureDosesForDate(s, today);
                    }
                });

        var rows = doseRepo.findTodayRows(clientUuid, today);

        int total = rows.size();
        int taken = (int) rows.stream().filter(r -> r.getStatus() == HEADDoseStatus.TAKEN).count();

        var grouped = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        HEADDoseTodayRow::getMedicationId,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        var meds = grouped.entrySet().stream().map(e -> {
            var medRows = e.getValue();
            var first = medRows.get(0);

            var doses = medRows.stream().map(r ->
                    new HEADMedicationsTodayResponse.Dose(
                            r.getDoseId(),
                            r.getDoseTime().toString(), // "08:00"
                            r.getStatus()
                    )
            ).toList();

            var emoji = first.getMedForm() != null ? first.getMedForm().getEmoji() : "💊";

            return new HEADMedicationsTodayResponse.MedicationCard(
                    first.getMedicationId(),
                    first.getMedicationName(),
                    first.getMedicationDosage(),
                    emoji,
                    doses
            );
        }).toList();

        return new HEADMedicationsTodayResponse(today, taken, total, meds);
    }

    @Transactional
    public void updateDoseStatus(long doseId, HEADDoseStatus newStatus) {
        String clientUuid = jwt.getUserNamePersonalUser();

        var dose = doseRepo.findByIdAndClientUuid(doseId, clientUuid)
                .orElseThrow(() -> new IllegalArgumentException("Dose not found"));

        dose.setStatus(newStatus);
        dose.setUpdatedAt(Instant.now());
        dose.setTakenAt(newStatus == HEADDoseStatus.TAKEN ? Instant.now() : null);

        doseRepo.save(dose);
    }
}