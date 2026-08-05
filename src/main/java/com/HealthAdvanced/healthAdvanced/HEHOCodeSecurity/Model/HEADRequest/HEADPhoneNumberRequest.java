package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADPhoneNumberRequest {
    private String phoneNumber;
    private Boolean isVerifyEmail;
}
