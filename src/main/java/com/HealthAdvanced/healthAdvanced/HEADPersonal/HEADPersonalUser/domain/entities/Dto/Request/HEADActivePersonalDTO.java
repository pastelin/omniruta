package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADActivePersonalDTO {
    private Long idActivepersonal;
    private HEADPersonalUserDTO idPersonalUser;
    private HEADClients idUserClient;
    private Double latitud;
    private Double longitud;
    private String dateCurrent;
    private Boolean isRejected;
    private String uuIdClient;
    private String idSocketClient;

    public HEADActivePersonalDTO(HEADActivePersonal activePersonal) {
        this.idActivepersonal = activePersonal.getIdActivePersonal();
        this.idPersonalUser = new HEADPersonalUserDTO(activePersonal.getIdPersonalUser());
        this.idUserClient = activePersonal.getIdUserClient();
        this.latitud = activePersonal.getLatitude();
        this.longitud = activePersonal.getLongitude();
        this.dateCurrent = activePersonal.getDateCurrent();
        this.isRejected = activePersonal.getIsRejected();
    }
}
