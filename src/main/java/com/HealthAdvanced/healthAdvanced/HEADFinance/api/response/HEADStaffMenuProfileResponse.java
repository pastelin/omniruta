package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

public record HEADStaffMenuProfileResponse(
        String name,
        String role,
        String photoUrl,
        String todayEarnings
) {}
