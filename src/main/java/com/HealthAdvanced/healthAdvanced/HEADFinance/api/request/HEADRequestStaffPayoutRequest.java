package com.HealthAdvanced.healthAdvanced.HEADFinance.api.request;


import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;

import java.time.Instant;

public record HEADRequestStaffPayoutRequest(
        HEADPayoutPeriodType periodType,
        Instant customFrom,
        Instant customTo,
        String currency
) {}