package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import org.springframework.web.multipart.MultipartFile;

public record HEADUploadClientRequest(MultipartFile file, HEADCategory headCategory,  Integer idDocumentCatalogue) {
}
