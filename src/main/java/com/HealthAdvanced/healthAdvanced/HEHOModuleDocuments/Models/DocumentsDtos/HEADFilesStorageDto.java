package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.DocumentsDtos;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADScreenType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import org.springframework.web.multipart.MultipartFile;

public record HEADFilesStorageDto(
        MultipartFile file, String folder,
        HEADOwnerType ownerType,
        Long ownerId,
        HEADCategory category,
        HEADScreenType headScreenType,
        HEADVisibility visibility,
        Integer idDocumentCatalogue,
        String subtitle,
        String title,
        String tags
) {
}
