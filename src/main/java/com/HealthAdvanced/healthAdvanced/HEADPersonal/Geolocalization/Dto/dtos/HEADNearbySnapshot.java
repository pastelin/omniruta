package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADNearbySnapshot {
    private String watchId;                    // para correlación/STOP
    private List<HEADStaffActiveCurrent> list; // ya tienes este DTO armado
    private long ts;
}
