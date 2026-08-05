package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADCodeSecurityResponse {
    private boolean ok;
    private HEADJwtUsersResponse headJwtUsersResponse;
}
