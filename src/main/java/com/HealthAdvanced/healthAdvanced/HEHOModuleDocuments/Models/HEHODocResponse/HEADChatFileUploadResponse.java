package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse;

public record HEADChatFileUploadResponse(
        Long fileAssetId,
        String fileName,
        String contentType,
        Long sizeBytes,
        String downloadUrl,
        String thumbnailUrl,
        Long contentLength
) {}