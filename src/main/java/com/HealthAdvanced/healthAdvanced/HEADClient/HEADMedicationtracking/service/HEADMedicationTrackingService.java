package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model.HEADMedicationDose;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model.HEADMedicationSchedule;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationDoseRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationScheduleRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADFrequencyMode;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADMedicationTrackingService {

    private final HEADMedicationScheduleRepository scheduleRepo;
    private final HEADMedicationDoseRepository doseRepo;

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");

    @Transactional
    public void onPrescriptionIssued(HEADPrescription prescription) {
        String clientUuid = prescription.getClientUuid();
        LocalDate today = LocalDate.now(ZONE);

        prescription.getMedications().stream()
                .map(med -> getOrCreateSchedule(prescription, med, clientUuid, today))
                .filter(schedule -> isWithinRange(today, schedule.getStartDate(), schedule.getEndDate()))
                .forEach(schedule -> ensureDosesForDate(schedule, today));
    }

    @Transactional
    public void ensureDosesForDate(HEADMedicationSchedule schedule, LocalDate date) {

        // horas que deberían existir
        var desiredTimes = buildDoseTimes(schedule);

        // horas que ya existen (1 query)
        var existingTimes = new HashSet<>(doseRepo.findExistingDoseTimes(schedule.getId(), date));

        // crea solo las faltantes
        var missing = desiredTimes.stream()
                .filter(t -> !existingTimes.contains(t))
                .map(t -> {
                    HEADMedicationDose d = new HEADMedicationDose();
                    d.setSchedule(schedule);
                    d.setClientUuid(schedule.getClientUuid());
                    d.setDoseDate(date);
                    d.setDoseTime(t);
                    d.setStatus(HEADDoseStatus.PENDING);
                    return d;
                })
                .toList();

        if (!missing.isEmpty()) {
            doseRepo.saveAll(missing);
        }
    }

    private HEADMedicationSchedule getOrCreateSchedule(
            HEADPrescription prescription,
            HEADPrescriptionMedication med,
            String clientUuid,
            LocalDate today
    ) {
        return scheduleRepo.findByPrescriptionMedication_Id(med.getId())
                .orElseGet(() -> {
                    HEADMedicationSchedule s = new HEADMedicationSchedule();
                    s.setClientUuid(clientUuid);
                    s.setPrescription(prescription);
                    s.setPrescriptionMedication(med);

                    s.setFrequencyMode(med.getFrequencyMode());
                    s.setTimesPerDay(med.getTimesPerDay());
                    s.setIntervalHours(med.getIntervalHours());
                    s.setTimezone("America/Mexico_City");

                    LocalDate start = today;
                    int duration = (med.getDurationDays() == null ? 1 : Math.max(1, med.getDurationDays()));
                    s.setStartDate(start);
                    s.setEndDate(start.plusDays(duration - 1L));

                    return scheduleRepo.save(s);
                });
    }

    private boolean isWithinRange(LocalDate day, LocalDate start, LocalDate end) {
        return !day.isBefore(start) && !day.isAfter(end);
    }

    private List<LocalTime> buildDoseTimes(HEADMedicationSchedule schedule) {
        if (schedule.getFrequencyMode() == HEADFrequencyMode.TIMES_PER_DAY) {
            int n = schedule.getTimesPerDay() != null ? schedule.getTimesPerDay() : 1;
            return defaultTimesPerDay(n);
        } else {
            int h = schedule.getIntervalHours() != null ? schedule.getIntervalHours() : 24;
            return timesEveryHours(h);
        }
    }

    private List<LocalTime> defaultTimesPerDay(int n) {
        int times = Math.max(1, Math.min(n, 12));
        int startHour = 8;
        int endHour = 22; // no incluye madrugada
        int window = endHour - startHour; // 14 horas

        if (times == 1) return List.of(LocalTime.of(startHour, 0));

        double step = (double) window / (times - 1);

        return java.util.stream.IntStream.range(0, times)
                .mapToObj(i -> {
                    int hour = (int) Math.round(startHour + i * step);
                    return LocalTime.of(Math.min(hour, 23), 0);
                })
                .distinct()
                .toList();
    }

    private List<LocalTime> timesEveryHours(int intervalHours) {
        int h = Math.max(1, Math.min(intervalHours, 24));
        LocalTime base = LocalTime.of(8, 0);

        // 0, h, 2h, ...
        return java.util.stream.IntStream.iterate(0, i -> i < 24, i -> i + h)
                .mapToObj(base::plusHours)
                .toList();
    }

    @Transactional
    public void ensurePrescriptionDosesUpToToday(Long prescriptionId) {
        LocalDate today = LocalDate.now(ZONE);

        scheduleRepo.findAllByPrescription_Id(prescriptionId).stream()
                .filter(HEADMedicationSchedule::isActive)
                .filter(s -> !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
                .forEach(this::ensureDosesUpToToday);
    }

    @Transactional
    public void ensureDosesUpToToday(HEADMedicationSchedule schedule) {
        LocalDate today = LocalDate.now(ZONE);

        if (today.isBefore(schedule.getStartDate())) return;

        LocalDate scheduleTo = today.isAfter(schedule.getEndDate()) ? schedule.getEndDate() : today;

        // ✅ límite para no generar cientos de días
        int MAX_DAYS_BACKFILL = 30;
        LocalDate scheduleFrom = schedule.getStartDate();

        LocalDate limitedFrom = scheduleTo.minusDays(MAX_DAYS_BACKFILL - 1L);
        LocalDate from = scheduleFrom.isAfter(limitedFrom) ? scheduleFrom : limitedFrom;

        for (LocalDate d = from; !d.isAfter(scheduleTo); d = d.plusDays(1)) {
            ensureDosesForDate(schedule, d);
        }
    }
}