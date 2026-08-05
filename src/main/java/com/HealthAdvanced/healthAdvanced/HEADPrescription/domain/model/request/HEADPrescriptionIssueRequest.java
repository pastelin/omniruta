package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.request;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;

public record HEADPrescriptionIssueRequest(
        String nameStaff,
        String licenceNo,
        Long jobId,
        HEADSignatureVectorDto signature
) {}
