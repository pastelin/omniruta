package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest;

public record TelnyxVerifyOtpRequest(
        String code,
        String verify_profile_id
) {}