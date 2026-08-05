package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import java.math.BigDecimal;
import java.time.Instant;

public record HEADStaffPayoutResponse(
        Long id,
        String status,
        BigDecimal amount,
        String currency,
        int itemCount,
        Instant requestedAt,
        Instant paidAt
) {}