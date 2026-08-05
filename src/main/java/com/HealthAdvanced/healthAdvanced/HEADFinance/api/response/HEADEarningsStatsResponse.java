package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

public record HEADEarningsStatsResponse(
        int totalServices,
        double averageAmount,
        int hoursWorked
) {

}
