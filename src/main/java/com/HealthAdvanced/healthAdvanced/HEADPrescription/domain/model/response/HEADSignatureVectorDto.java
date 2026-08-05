package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

import java.util.List;

public record HEADSignatureVectorDto(
        int version,
        Float width,
        Float height,
        List<HEADSignatureStrokeDto> strokes
) {}