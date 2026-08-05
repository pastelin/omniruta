package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Request;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADOccupationsProfileRequest {
    private Integer IdOccupationProfile;
    private String nameTypeProfile;
    private HEADOccupations idOccupation;
}
