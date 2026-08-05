package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.Dto;

public record HEADRouteDto(
        long distanceMeters,
        long durationSeconds,
        String startAddress,
        String endAddress,
        String polyline,
        double northLat,
        double eastLng,
        double southLat,
        double westLng
) {}


