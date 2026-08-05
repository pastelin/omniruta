package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADActivePersonalDTO;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "activePersonal")
public class HEADActivePersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActivePersonal;
    @ManyToOne
    private HEADPersonalUser idPersonalUser;
    @ManyToOne
    private HEADClients idUserClient;
    private Double latitude;
    private Double longitude;
    private String dateCurrent;
    private Boolean isRejected;
    private String uuIdClient;
    private String idSocketClient;

    public HEADActivePersonal(HEADActivePersonalDTO activePersonalDTO) {
        this.setIdPersonalUser(new HEADPersonalUser(activePersonalDTO.getIdPersonalUser(), null));
        this.setIdUserClient(activePersonalDTO.getIdUserClient());
        this.setLatitude(activePersonalDTO.getLatitud());
        this.setLongitude(activePersonalDTO.getLongitud());
        this.setDateCurrent(activePersonalDTO.getDateCurrent());
        this.setUuIdClient(activePersonalDTO.getUuIdClient());
        this.setIdSocketClient(activePersonalDTO.getIdSocketClient());
    }
}
