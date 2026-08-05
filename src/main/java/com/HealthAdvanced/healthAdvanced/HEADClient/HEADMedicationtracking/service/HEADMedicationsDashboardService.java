package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response.HEADMedicationsDashboardResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADDoseDailyAggView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository.HEADMedicationDoseRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADMedicationsDashboardService {

    private final HEADMedicationDoseRepository doseRepo;
    private final HEADJwtGenerator jwt;

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");
    private static final int ADHERENCE_DAYS = 30;
    private static final int STREAK_LOOKBACK_DAYS = 60; // para calcular racha

    @Transactional(readOnly = true)
    public HEADMedicationsDashboardResponse dashboard() {
        String clientUuid = jwt.getUserNamePersonalUser();

        ZonedDateTime nowZ = ZonedDateTime.now(ZONE);
        LocalDate today = nowZ.toLocalDate();
        LocalTime nowTime = nowZ.toLocalTime().withSecond(0).withNano(0);

        // ---- Hoy ----
        int totalToday = safeInt(doseRepo.countByClientUuidAndDoseDate(clientUuid, today));
        int takenToday = safeInt(doseRepo.countByClientUuidAndDoseDateAndStatus(clientUuid, today, HEADDoseStatus.TAKEN));
        int progressToday = (totalToday == 0) ? 0 : (int) Math.round((takenToday * 100.0) / totalToday);

        // ---- Próxima dosis (hoy) ----
        HEADMedicationsDashboardResponse.NextDose nextDose = buildNextDose(clientUuid, today, nowTime, nowZ);

        // ---- Adherencia 30 días + Streak ----
        LocalDate from = today.minusDays(ADHERENCE_DAYS - 1L);
        LocalDate streakFrom = today.minusDays(STREAK_LOOKBACK_DAYS - 1L);

        // una sola query diaria para streak (60d) y luego usamos parte para adherencia (30d)
        List<HEADDoseDailyAggView> daily = doseRepo.aggregateDaily(clientUuid, streakFrom, today);

        int streakDays = computeStreak(daily, today);
        int adherencePercent = computeAdherencePercent(daily, from, today);

        return new HEADMedicationsDashboardResponse(
                today,
                progressToday,
                takenToday,
                totalToday,
                nextDose,
                streakDays,
                adherencePercent
        );
    }

    private HEADMedicationsDashboardResponse.NextDose buildNextDose(
            String clientUuid,
            LocalDate today,
            LocalTime nowTime,
            ZonedDateTime nowZ
    ) {
        var rows = doseRepo.findNextPendingToday(clientUuid, today, nowTime);
        if (rows.isEmpty()) return null;

        var r = rows.get(0);

        // minutos hasta esa hora (mismo día)
        LocalTime t = r.getDoseTime();
        long minutesUntil = Duration.between(nowZ.toLocalTime().withSecond(0).withNano(0), t).toMinutes();
        if (minutesUntil < 0) minutesUntil = 0;

        String emoji = (r.getMedForm() != null) ? r.getMedForm().getEmoji() : "💊";

        return new HEADMedicationsDashboardResponse.NextDose(
                r.getDoseId(),
                r.getMedicationId(),
                r.getMedicationName(),
                r.getMedicationDosage(),
                emoji,
                t.toString(),      // "16:00"
                minutesUntil
        );
    }

    // Día “cumplido” = no hay pendientes y total > 0
    // (si quieres que SKIPPED cuente como resuelto, está cubierto porque solo revisamos PENDING)
    private int computeStreak(List<HEADDoseDailyAggView> dailyDesc, LocalDate today) {
        int streak = 0;
        LocalDate expected = today;

        for (var d : dailyDesc) {

            if (!d.getDoseDate().equals(expected)) break;

            boolean hasDoses = d.getTotal() > 0;
            boolean noPending = d.getPending() == 0;

            if (hasDoses && noPending) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private int computeAdherencePercent(List<HEADDoseDailyAggView> dailyDesc, LocalDate from, LocalDate to) {
        long total = 0;
        long taken = 0;

        for (var d : dailyDesc) {
            LocalDate day = d.getDoseDate();
            if (day.isBefore(from) || day.isAfter(to)) continue;
            total += d.getTotal();
            taken += d.getTaken();
        }

        if (total == 0) return 0;
        return (int) Math.round((taken * 100.0) / total);
    }

    private int safeInt(long v) {
        return (v > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) v;
    }
}