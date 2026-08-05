package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse;

public record TelnyxVerifyOtpResponse(
        Data data
) {
    public record Data(
            String phone_number,
            String response_code
    ) {}
}