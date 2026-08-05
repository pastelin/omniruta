package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HEADGoogleLeg(
        HEADGoogleValueText distance,
        HEADGoogleValueText duration,

        @JsonProperty("start_address")
        String startAddress,

        @JsonProperty("end_address")
        String endAddress
) {}