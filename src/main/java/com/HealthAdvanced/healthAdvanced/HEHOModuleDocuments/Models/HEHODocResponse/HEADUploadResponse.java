package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADUploadResponse {
    private boolean success;
    private String message;
    private String url;        // puede ser null si es privado
    private String status;     // "PENDING"
    private Integer documentId;
}
