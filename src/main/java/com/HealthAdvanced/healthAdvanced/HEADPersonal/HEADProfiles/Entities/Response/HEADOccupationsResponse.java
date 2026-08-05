package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADOccupationsResponse {
    private Integer idOccupation;
    private String nameOccupation;
    private List<HEADOccupationProfileResponse> headOccupationsProfiles;
}
