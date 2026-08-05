package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;

import java.math.BigDecimal;

public record HEADEarningsSummaryResponse(
        BigDecimal totalEarned,
        String currency,
        String growthLabel,
        long totalServices,
        BigDecimal averagePerService,
        BigDecimal availableToWithdraw,
        Boolean canWithdraw,
        long totalWorkedHours,
        HEADPageResponse<HEADEarningTransactionResponse> transactions
) {}