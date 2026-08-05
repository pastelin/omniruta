package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HEADNearbyWatchReq {
    private double lat;
    private double lng;
    private int    limit        = 50;
    private boolean useEta      = true;
    private double avgSpeedKmh  = 15.0;
    private Set<Long>   filterProfileIds   = Set.of();
    private Long packageSlug; // opcional ("medico_general", "enfermeria_aux")
}
