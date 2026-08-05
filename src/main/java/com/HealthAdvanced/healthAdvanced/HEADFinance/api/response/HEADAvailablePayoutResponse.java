package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import java.math.BigDecimal;

public record HEADAvailablePayoutResponse(
        BigDecimal availableAmount,
        String currency,
        String periodLabel
) {}
