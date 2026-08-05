package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model;

import java.math.BigDecimal;

public record HEADFeeCalculationResult(
        BigDecimal grossAmount,
        BigDecimal platformFeePercent,
        BigDecimal platformFeeFixed,
        BigDecimal platformFeeAmount,
        BigDecimal processorFeePercent,
        BigDecimal processorFeeFixed,
        BigDecimal processorFeeAmount,
        BigDecimal staffPayoutBeforeWithholding,
        BigDecimal withholdingAmount,
        BigDecimal staffPayoutAmount,
        BigDecimal appNetAmount
) {}