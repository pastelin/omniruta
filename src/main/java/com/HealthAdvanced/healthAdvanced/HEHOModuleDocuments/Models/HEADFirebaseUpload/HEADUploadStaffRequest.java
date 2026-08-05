package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import org.springframework.web.multipart.MultipartFile;

public record HEADUploadStaffRequest(
        MultipartFile file,
        HEADCategory headCategory,
        Integer idDocumentCatalogue,
        Long occupationProfileId,
        String licenseNo) {
}
