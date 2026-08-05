package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.dtos;

import java.util.List;

public record HEADNearbySubscribeDto(
        double lat,
        double lng,
        double radiusMeters,
        int limit,
        String requestId,
        List<Long> profileIds // filtros multi-servicio
) {
}
