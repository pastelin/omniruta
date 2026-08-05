package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADOccupationProfileResponse {
    private Long IdOccupationProfile;
    private String nameTypeProfile;
    private Integer idOccupation;
}
