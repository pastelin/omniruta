package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADOccupationsProfilesDto {
    private List<HEADOccupationsResponse> headOccupations;
}
