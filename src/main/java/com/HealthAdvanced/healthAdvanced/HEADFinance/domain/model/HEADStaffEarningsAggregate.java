package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model;

import java.math.BigDecimal;

public record HEADStaffEarningsAggregate(
        Long totalJobs,
        BigDecimal totalEarned,
        Long totalDurationSeconds
) {}