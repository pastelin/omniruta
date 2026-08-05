package com.HealthAdvanced.healthAdvanced.HEADAdmin.controller;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity.HEADChangeStatusDocument;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEADFileStorageService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadSystemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/files/v1")
public class HEADSystemUpdateInfoController {

    @Autowired
    private HEADFileStorageService headFileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSystem(@ModelAttribute HEADUploadSystemRequest headUploadSystemRequest) throws Exception {
        return headFileStorageService.uploadSystemAsset(headUploadSystemRequest); // BANNER, ICON, IMAGE
    }

    @PostMapping("/changeStatusStaff")
    public ResponseEntity<?> changeStatusStaff(@RequestBody HEADChangeStatusDocument headChangeStatusDocument) {
        headFileStorageService.approveDocument(headChangeStatusDocument);
        return ResponseEntity.ok().build();
    }
}
