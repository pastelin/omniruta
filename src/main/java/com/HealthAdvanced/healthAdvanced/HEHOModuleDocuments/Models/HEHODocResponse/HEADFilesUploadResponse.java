package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse;

import lombok.Data;

import java.util.List;

@Data
public class HEADFilesUploadResponse {
    private List<HEHOFileUploadResponse> resultFiles;
    public HEADFilesUploadResponse(List<HEHOFileUploadResponse> resultFiles) {
        this.resultFiles = resultFiles;
    }
}
