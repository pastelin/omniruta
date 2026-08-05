package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class HEADNearbyDelta {
    private List<HEADStaffActiveCurrent> added   = List.of();
    private List<HEADStaffActiveCurrent> updated = List.of();
    private List<String>                  removed = List.of();
}
