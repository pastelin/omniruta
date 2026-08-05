package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

public record HEADGoogleBounds(
        HEADGoogleLatLng northeast,
        HEADGoogleLatLng southwest
) {}