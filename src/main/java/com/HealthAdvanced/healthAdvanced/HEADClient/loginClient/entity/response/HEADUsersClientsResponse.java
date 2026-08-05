package com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStepCurrentPersonalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADUsersClientsResponse {
    private String name;
    private String firstName;
    private String lastName;
    private String accessToken;
    private String email;
    private String numberPhone;
    private long expiresAt;
}
