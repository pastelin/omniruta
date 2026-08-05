package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HEADWebSocketEntity {
    private HEADWebSocketUsersEntity headWebSocketUsersEntity;
}
