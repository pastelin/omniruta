package com.HealthAdvanced.healthAdvanced.HEADLegal.api.response;

import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;

public record HEADLegalDocumentResponse(
        Long id,
        HEADLegalUserType userType,
        HEADLegalDocumentType documentType,
        String title,
        String version,
        String contentUrl,
        String contentText,
        String language
) {}