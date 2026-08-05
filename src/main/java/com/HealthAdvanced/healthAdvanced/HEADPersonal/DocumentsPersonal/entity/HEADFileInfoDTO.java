package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADFileInfoDTO {
    private Long fileAssetId;
    private String url;              // null si es privado
    private String storageKey;
    private String mimeType;
    private Long sizeBytes;
}
