package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningTransactionStatus;

public record HEADEarningTransactionItemResponse(
        Long id,
        String type,
        String patient,
        double amount,
        String date,
        String time,
        HEADEarningTransactionStatus status
) {
}
