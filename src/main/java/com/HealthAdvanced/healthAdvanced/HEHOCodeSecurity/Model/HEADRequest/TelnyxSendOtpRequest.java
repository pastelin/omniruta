package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest;

public record TelnyxSendOtpRequest(
        String phone_number,
        String verify_profile_id
) {}