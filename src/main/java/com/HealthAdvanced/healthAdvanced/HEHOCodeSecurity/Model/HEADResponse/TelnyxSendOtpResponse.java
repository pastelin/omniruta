package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse;

public record TelnyxSendOtpResponse(
        Data data
) {
    public record Data(
            String id,
            String phone_number,
            String verify_profile_id,
            String status,
            Integer timeout_secs,
            Integer failed_attempts
    ) {}
}