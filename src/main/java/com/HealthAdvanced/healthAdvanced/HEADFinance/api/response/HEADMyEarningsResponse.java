package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningsPeriod;

import java.util.List;

public record HEADMyEarningsResponse(
        HEADEarningsPeriod selectedPeriod,
        double weeklyEarnings,
        double monthlyEarnings,
        double yearlyEarnings,
        int growthPercentage,
        HEADEarningsStatsResponse stats,
        List<HEADEarningTransactionItemResponse> transactions,
        HEADPaymentMethodResponse paymentMethod
) {}