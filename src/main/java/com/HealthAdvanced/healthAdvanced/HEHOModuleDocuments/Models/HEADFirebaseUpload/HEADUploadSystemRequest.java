package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADScreenType;
import org.springframework.web.multipart.MultipartFile;

public record HEADUploadSystemRequest(MultipartFile file, HEADCategory headCategory, HEADScreenType headScreenType, String title, String subtitle, String tags, Long ownerId) {
}
