package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HEADGoogleDirectionsResponse(
        List<HEADGoogleRoute> routes,
        String status
) {}
