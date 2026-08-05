package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADNearbyStartReq {
    private double userLat;
    private double userLng;
    private double radiusMeters;       // ej. 3000
    private String packageSlug;        // ej. "pkg_medico_general_consulta"
    private boolean useEta = true;
    private double avgSpeedKmh = 25.0;
}
