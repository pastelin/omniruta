package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADPersonalUserDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HEADJwtUsersResponse {
    @JsonIgnore
    private Long idUser;
    @JsonIgnore
    private String uidUser;
    private String name;
    private String firstName;
    private String lastName;
    private Boolean isAccepted;
    private String accessToken;
    private long expiresAt;
    private String refreshToken;
    private String email;
    private String numberPhone;
    private Boolean isExistsPersonal;
    private HEADStatusResponseDTO stepCurrent;
    private HEADAppStateDTO headAppStateDTO;

    public HEADJwtUsersResponse(HEADPersonalUserDTO headPersonalUserDTO) {
        this.uidUser = headPersonalUserDTO.getUidUser();
        this.name = headPersonalUserDTO.getNombre();
        this.firstName = headPersonalUserDTO.getPaterno();
        this.lastName = headPersonalUserDTO.getMaterno();
        this.isAccepted = headPersonalUserDTO.getIsAccepted();
        this.email = headPersonalUserDTO.getEmail();
        this.numberPhone = headPersonalUserDTO.getTelefono();
        this.stepCurrent = headPersonalUserDTO.getHeadStepCurrentPersonal();
        this.isExistsPersonal = headPersonalUserDTO.getIsExistsPersonal();
    }
}
