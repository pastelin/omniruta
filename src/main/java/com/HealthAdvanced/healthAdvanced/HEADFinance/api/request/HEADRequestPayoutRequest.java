package com.HealthAdvanced.healthAdvanced.HEADFinance.api.request;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record HEADRequestPayoutRequest(
        @NotNull
        HEADPayoutPeriodType periodType,

        Instant customFrom,

        Instant customTo,

        @NotBlank
        String currency
) {}