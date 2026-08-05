package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import java.math.BigDecimal;

public record HEADEarningTransactionResponse(
        Long id,
        String serviceName,
        String patientName,
        BigDecimal amount,
        String status,
        String date,
        String time
) {}
