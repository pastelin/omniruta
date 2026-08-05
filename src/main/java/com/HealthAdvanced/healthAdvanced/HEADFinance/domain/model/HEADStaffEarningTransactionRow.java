package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record HEADStaffEarningTransactionRow(
        Long jobId,
        String serviceName,
        String nombre,
        String paterno,
        BigDecimal amount,
        String payoutStatus,
        Instant completedAt
) {}