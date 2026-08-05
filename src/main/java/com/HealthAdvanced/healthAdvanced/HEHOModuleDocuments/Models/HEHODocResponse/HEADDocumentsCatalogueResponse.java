package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse;

import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import lombok.Data;

import java.util.List;

@Data
public class HEADDocumentsCatalogueResponse {
    private List<HEADDocumentCatalogue> resultDocumentsCatalogue;
    public HEADDocumentsCatalogueResponse(List<HEADDocumentCatalogue> resultDocumentsCatalogue) {
        this.resultDocumentsCatalogue = resultDocumentsCatalogue;
    }
}
