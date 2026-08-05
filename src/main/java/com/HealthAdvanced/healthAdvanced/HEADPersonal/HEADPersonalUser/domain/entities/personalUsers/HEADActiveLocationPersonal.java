package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "activeLocationPersonal")
public class HEADActiveLocationPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActivePersonal;
    @ManyToOne
    private HEADPersonalUser idPersonalUser;
    private Double latitude;
    private Double longitude;
    private Boolean isActiveWork;
    private Boolean isBusy;
    private String dateCurrent;
    private String uuIdPersonal;
    private String idSocketPersonal;

    public HEADActiveLocationPersonal(HEADWebSocketUsersEntity headWebSocketUsersEntity,
                                      HEADPersonalUser idPersonalUser) {
        this.idPersonalUser = idPersonalUser;
        this.latitude = headWebSocketUsersEntity.getLatitude();
        this.longitude = headWebSocketUsersEntity.getLongitude();
        this.isActiveWork = headWebSocketUsersEntity.getIsActiveWork();
        this.isBusy = headWebSocketUsersEntity.getIsBusy();
        this.dateCurrent = HEADCommonsUtils.getDateTimeCurrent();
        this.uuIdPersonal = headWebSocketUsersEntity.getUuIdPersonal();
        this.idSocketPersonal = headWebSocketUsersEntity.getIdSocketUser();
    }
}
