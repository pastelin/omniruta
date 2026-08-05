package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHOMaps;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocuments;
import org.springframework.stereotype.Service;


@Service
public class HEADDocumentsMaps {

    public HEADDocuments headDocumentsMap(HEADDocumentCatalogue headDocumentCatalogue,String extension, HEADPersonalUser headPersonalUser) {
        HEADDocuments headDocuments = new HEADDocuments();
        headDocuments.setNombreArchivo(headDocumentCatalogue.getNameDocument());
        headDocuments.setIdDocument(headDocumentCatalogue);
        headDocuments.setExtension(extension);
        headDocuments.setIdUser(headPersonalUser);
        return headDocuments;
    }

    public String folderFile(HEADCategory headCategory) {
        return switch (headCategory) {
            case BANNER -> "system/banners";
            case SERVICE_ICON -> "system/icons";
            case IMAGE -> "system/images";
            case PROMO_CARD -> "system/promos_card";
            case CATEGORIES -> "system/categories";
            case ICONS_SERVICES -> "system/icons_services";
            case PACKAGE_ICON -> "system/package_icon";
            case PACKAGE_IMAGE -> "system/package_image";
            case MAP_PIN_SYSTEM -> "system/map_pin";
            default -> "system/files";
        };
    }
}
