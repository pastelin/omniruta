package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;


import java.util.List;

public record HEADSignatureStrokeDto(
        List<HEADSignaturePointDto> points
) {}
