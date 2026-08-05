package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADPrescriptionRangeView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationDoseRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationScheduleRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service.HEADMedicationTrackingService;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADPrescriptionProgressService {

    private final HEADMedicationScheduleRepository scheduleRepo;
    private final HEADMedicationDoseRepository doseRepo;
    private final HEADMedicationTrackingService trackingService;

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");

    @Transactional
    public Map<Long, Integer> progressPercentByPrescriptionIds(String clientUuid, List<Long> prescriptionIds) {
        if (prescriptionIds == null || prescriptionIds.isEmpty()) return Map.of();

        // 1) asegurar doses up-to-today por receta (evita totals incompletos)
        prescriptionIds.forEach(trackingService::ensurePrescriptionDosesUpToToday);

        // 2) rangos por receta (min start, max end)
        var ranges = scheduleRepo.findRangesByClientAndPrescriptionIds(clientUuid, prescriptionIds);

        LocalDate today = LocalDate.now(ZONE);

        // build map ranges
        Map<Long, Range> rangeMap = ranges.stream()
                .collect(Collectors.toMap(
                        HEADPrescriptionRangeView::getPrescriptionId,
                        r -> new Range(
                                r.getStartDate(),
                                r.getEndDate().isAfter(today) ? today : r.getEndDate()
                        )
                ));

        // 3) rango global para 1 query
        LocalDate globalFrom = rangeMap.values().stream()
                .map(Range::from)
                .min(LocalDate::compareTo)
                .orElse(today);

        LocalDate globalTo = rangeMap.values().stream()
                .map(Range::to)
                .max(LocalDate::compareTo)
                .orElse(today);

        // 4) 1 query: totals/taken por receta y día
        var dayAgg = doseRepo.aggregateByPrescriptionAndDay(clientUuid, prescriptionIds, globalFrom, globalTo);

        // 5) sumar dentro del rango de cada receta
        Map<Long, long[]> acc = new HashMap<>(); // [0]=taken, [1]=total

        dayAgg.forEach(row -> {
            Long pid = row.getPrescriptionId();
            Range rg = rangeMap.get(pid);
            if (rg == null) return;

            LocalDate d = row.getDoseDate();
            if (d.isBefore(rg.from()) || d.isAfter(rg.to())) return;

            long[] a = acc.computeIfAbsent(pid, __ -> new long[]{0L, 0L});
            a[0] += row.getTaken();
            a[1] += row.getTotal();
        });

        // 6) percent
        Map<Long, Integer> out = new HashMap<>();
        prescriptionIds.forEach(pid -> {
            long[] a = acc.get(pid);
            if (a == null || a[1] == 0) {
                out.put(pid, 0);
            } else {
                out.put(pid, (int) Math.round((a[0] * 100.0) / a[1]));
            }
        });

        return out;
    }

    private record Range(LocalDate from, LocalDate to) {}
}
