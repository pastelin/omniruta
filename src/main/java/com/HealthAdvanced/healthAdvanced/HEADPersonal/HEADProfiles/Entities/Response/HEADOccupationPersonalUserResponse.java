package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADOccupationPersonalUserResponse {
    private Boolean isSaveOccupationSelected;
    private Integer selectedCount;
    private HEADStatusResponseDTO stepCurrent;
    private HEADAppStateDTO headAppStateDTO;
}