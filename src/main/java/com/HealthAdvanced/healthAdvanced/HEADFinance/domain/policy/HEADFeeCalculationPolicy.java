package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.policy;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPaymentProcessorRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPlatformFeeRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFeeBearer;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFeeCalculationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HEADFeeCalculationPolicy {

    public HEADFeeCalculationResult calculate(
            BigDecimal grossAmount,
            HEADPlatformFeeRule platformRule,
            HEADPaymentProcessorRule processorRule,
            BigDecimal directOperationalCostAmount,
            BigDecimal withholdingAmount
    ) {
        BigDecimal gross = safe(grossAmount);
        BigDecimal directCost = safe(directOperationalCostAmount);
        BigDecimal withholding = safe(withholdingAmount);

        BigDecimal platformFeeAmount = percentage(gross, platformRule.getPlatformFeePercent())
                .add(safe(platformRule.getPlatformFeeFixed()));

        BigDecimal processorFeeAmount = percentage(gross, processorRule.getPercentFee())
                .add(safe(processorRule.getFixedFee()));

        BigDecimal staffBefore = gross.subtract(platformFeeAmount);
        BigDecimal appNet = platformFeeAmount;

        if (processorRule.getFeeBearer() == HEADFeeBearer.STAFF) {
            staffBefore = staffBefore.subtract(processorFeeAmount);
        } else if (processorRule.getFeeBearer() == HEADFeeBearer.PLATFORM) {
            appNet = appNet.subtract(processorFeeAmount);
        }

        appNet = appNet.subtract(directCost);

        BigDecimal staffPayout = staffBefore.subtract(withholding);

        return new HEADFeeCalculationResult(
                scale(gross),
                scale(platformRule.getPlatformFeePercent()),
                scale(safe(platformRule.getPlatformFeeFixed())),
                scale(platformFeeAmount),
                scale(processorRule.getPercentFee()),
                scale(safe(processorRule.getFixedFee())),
                scale(processorFeeAmount),
                scale(staffBefore),
                scale(withholding),
                scale(staffPayout),
                scale(appNet)
        );
    }

    private BigDecimal percentage(BigDecimal base, BigDecimal percent) {
        return safe(base)
                .multiply(safe(percent))
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }
}