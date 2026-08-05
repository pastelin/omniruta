package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload;

import com.google.cloud.storage.Bucket;

public record HEADUploadResult(String storageKey, String url, String contentType, long sizeBytes, Bucket bucket) {
}
