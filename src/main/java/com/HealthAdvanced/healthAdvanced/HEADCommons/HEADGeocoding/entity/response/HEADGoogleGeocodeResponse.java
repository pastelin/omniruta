package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import java.util.List;

public record HEADGoogleGeocodeResponse(
        List<HEADGoogleResult> results,
        String status
) { }
