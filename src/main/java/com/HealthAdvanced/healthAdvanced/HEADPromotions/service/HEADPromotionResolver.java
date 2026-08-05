package com.HealthAdvanced.healthAdvanced.HEADPromotions.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADResolvedPromos;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADResolvedPromotion;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromoMetricKey;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionRule;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.repository.HEADPromotionRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.repository.HEADPromotionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADPromotionResolver {

    private final HEADPromotionRepository promoRepo;
    private final HEADPromotionRuleRepository ruleRepo;
    private final HEADPromotionMetricsService metricsService;
    private final HEADPromotionRuleEvaluator evaluator;
    private final HEADJwtGenerator jwt;

    @Transactional(readOnly = true)
    public HEADResolvedPromos resolveForProfileAndPackages(Long profileId, List<String> packageIds) {

        String clientUuid = jwt.getUserNamePersonalUser();
        if (clientUuid == null || clientUuid.isBlank()) return HEADResolvedPromos.empty();

        if (profileId == null) return HEADResolvedPromos.empty();
        if (packageIds == null) packageIds = List.of();

        LocalDateTime now = LocalDateTime.now();

        // 0) metrics (solo si vamos a evaluar reglas)
        Map<HEADPromoMetricKey, BigDecimal> metrics = metricsService.computeForCurrentClient();

        // 1) promos candidatos (CATEGORY + PACKAGE)
        var targetsCategory = List.of(String.valueOf(profileId));
        var categoryPromos = promoRepo.findActiveForTargets(
                HEADPromotionTargetType.CATEGORY, targetsCategory, now, HEADPromotionStatus.ACTIVE);

        var packagePromos = packageIds.isEmpty()
                ? List.<HEADPromotion>of()
                : promoRepo.findActiveForTargets(
                HEADPromotionTargetType.PACKAGE, packageIds, now, HEADPromotionStatus.ACTIVE);

        var allCandidates = new java.util.ArrayList<HEADPromotion>();
        allCandidates.addAll(categoryPromos);
        allCandidates.addAll(packagePromos);

        if (allCandidates.isEmpty()) return HEADResolvedPromos.empty();

        // 2) traer reglas en batch
        var promoIds = allCandidates.stream().map(HEADPromotion::getId).toList();
        var rules = ruleRepo.findEnabledRulesForPromotions(promoIds);

        Map<Long, List<HEADPromotionRule>> rulesByPromo = rules.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getPromotion().getId()));

        // 3) filtrar solo promos que cumplen reglas
        var eligible = allCandidates.stream()
                .filter(p -> evaluator.matchesAllRules(rulesByPromo.get(p.getId()), metrics))
                .toList();

        if (eligible.isEmpty()) return HEADResolvedPromos.empty();

        // 4) pick best profile promo (CATEGORY) y best per package (PACKAGE)
        HEADPromotion bestProfilePromo = eligible.stream()
                .filter(p -> p.getTargetType() == HEADPromotionTargetType.CATEGORY
                        && String.valueOf(profileId).equals(p.getTargetId()))
                .max(java.util.Comparator.comparingInt(this::priority))
                .orElse(null);

        Map<String, HEADPromotion> bestByPackage = eligible.stream()
                .filter(p -> p.getTargetType() == HEADPromotionTargetType.PACKAGE)
                .filter(p -> p.getTargetId() != null && !p.getTargetId().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        HEADPromotion::getTargetId,
                        java.util.function.Function.identity(),
                        (a, b) -> priority(a) >= priority(b) ? a : b
                ));

        return new HEADResolvedPromos(bestProfilePromo, bestByPackage);
    }

    private int priority(HEADPromotion p) {
        return (p == null || p.getPriority() == null) ? 0 : p.getPriority();
    }
}