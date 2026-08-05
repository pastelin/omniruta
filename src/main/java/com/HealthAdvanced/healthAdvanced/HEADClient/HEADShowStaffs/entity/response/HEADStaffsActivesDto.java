package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADStaffsActivesDto {
    private List<HEADStaffActiveCurrent> headStaffsCurrents;
}
