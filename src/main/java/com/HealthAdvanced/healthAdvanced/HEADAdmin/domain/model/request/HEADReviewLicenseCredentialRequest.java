package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;

public record HEADReviewLicenseCredentialRequest(
        Long credentialId,
        String licenseNo,          // opcional: si admin corrige
        HEADDocumentStatus status, // APPROVED / REJECTED
        Long sourceDocumentId,     // opcional: el HEADDocuments.idDocs que aprobó
        String reviewNotes         // opcional
) {}
