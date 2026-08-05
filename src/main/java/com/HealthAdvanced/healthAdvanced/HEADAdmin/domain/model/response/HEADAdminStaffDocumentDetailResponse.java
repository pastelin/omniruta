package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HEADAdminStaffDocumentDetailResponse {

    private Long idDocs;
    private Integer documentCatalogueId;

    private String nombreArchivo;
    private String extension;

    private String url;
    private String mimeType;
    private Long sizeBytes;
    private String storageKey;

    private HEADDocumentStatus status;
    private String reviewNotes;
    private Long reviewedByAdminId;

    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;

    private Long occupationProfileId;
}
