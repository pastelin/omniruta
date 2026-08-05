package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HEADGoogleRoute(
        List<HEADGoogleLeg> legs,

        @JsonProperty("overview_polyline")
        HEADOverviewPolyline overviewPolyline,
        HEADGoogleBounds bounds
) {}