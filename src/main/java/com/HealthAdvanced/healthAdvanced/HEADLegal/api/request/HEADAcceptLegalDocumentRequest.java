package com.HealthAdvanced.healthAdvanced.HEADLegal.api.request;

public record HEADAcceptLegalDocumentRequest(
        Long legalDocumentId,
        String language
) {}