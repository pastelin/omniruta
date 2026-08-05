package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HEADOverviewPolyline(
        String points
) {}
