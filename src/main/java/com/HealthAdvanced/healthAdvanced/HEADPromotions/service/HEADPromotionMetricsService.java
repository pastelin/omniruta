package com.HealthAdvanced.healthAdvanced.HEADPromotions.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffReviewRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromoMetricKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HEADPromotionMetricsService {

    private final HEADJwtGenerator jwt;
    private final HEADClientsRepository clientsRepo;
    private final HEADJobRepository jobRepo;
    private final HEADStaffReviewRepository reviewRepo;

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");

    @Transactional(readOnly = true)
    public Map<HEADPromoMetricKey, BigDecimal> computeForCurrentClient() {

        String clientUuid = jwt.getUserNamePersonalUser();
        var client = clientsRepo.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Long clientId = client.getIdUser();

        // 1) COMPLETED_JOBS
        long completed = jobRepo.countByClient_IdUserAndState(clientId, HEADJobState.COMPLETED);

        // 2) LAST_COMPLETED_DAYS_AGO
        // (si no hay completados, ponemos un número alto o null; yo recomiendo null)
        Instant lastCompletedAt = jobRepo.findLastCompletedAt(clientId).orElse(null);
        BigDecimal lastDaysAgo = null;
        if (lastCompletedAt != null) {
            long days = java.time.Duration.between(lastCompletedAt, Instant.now()).toDays();
            lastDaysAgo = BigDecimal.valueOf(days);
        }

        // 3) AVG_RATING_LAST_90D
        Instant from90d = Instant.now().minus(90, ChronoUnit.DAYS);
        Double avg90 = reviewRepo.avgRatingForClientIdSince(clientId, from90d); // haz este query
        BigDecimal avgRating90d = (avg90 == null) ? null : BigDecimal.valueOf(avg90);

        var map = new EnumMap<HEADPromoMetricKey, BigDecimal>(HEADPromoMetricKey.class);
        map.put(HEADPromoMetricKey.COMPLETED_JOBS, BigDecimal.valueOf(completed));
        map.put(HEADPromoMetricKey.LAST_COMPLETED_DAYS_AGO, lastDaysAgo);
        map.put(HEADPromoMetricKey.AVG_RATING_LAST_90D, avgRating90d);

        return map;
    }
}