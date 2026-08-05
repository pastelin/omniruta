package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADModelEmail {
    private String email;
    private Boolean isVerifyPhone;
}
