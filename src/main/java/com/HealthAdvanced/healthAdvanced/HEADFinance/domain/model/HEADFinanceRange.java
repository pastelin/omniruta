package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model;

import java.time.Instant;

public record HEADFinanceRange(
        Instant from,
        Instant to
) {}
