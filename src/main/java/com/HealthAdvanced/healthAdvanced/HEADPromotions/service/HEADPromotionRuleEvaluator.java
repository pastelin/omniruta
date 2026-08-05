package com.HealthAdvanced.healthAdvanced.HEADPromotions.service;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromoMetricKey;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class HEADPromotionRuleEvaluator {

    public boolean matchesAllRules(List<HEADPromotionRule> rules,
                                   Map<HEADPromoMetricKey, BigDecimal> metrics) {

        if (rules == null || rules.isEmpty()) return true; // sin reglas = pasa

        return rules.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                .allMatch(r -> evalRule(r, metrics.get(r.getMetricKey())));
    }

    private boolean evalRule(HEADPromotionRule r, BigDecimal metricVal) {
        if (metricVal == null) return false; // si no hay valor -> no cumple

        BigDecimal v1 = r.getValue1();
        BigDecimal v2 = r.getValue2();

        return switch (r.getOperator()) {
            case EQ -> metricVal.compareTo(v1) == 0;
            case NE -> metricVal.compareTo(v1) != 0;
            case GT -> metricVal.compareTo(v1) > 0;
            case GTE -> metricVal.compareTo(v1) >= 0;
            case LT -> metricVal.compareTo(v1) < 0;
            case LTE -> metricVal.compareTo(v1) <= 0;
            case BETWEEN -> (v2 != null && metricVal.compareTo(v1) >= 0 && metricVal.compareTo(v2) <= 0);
        };
    }
}