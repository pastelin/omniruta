package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADOccupationsPersonalUserRequest {
    private List<Long> idOccupationProfile;
}
