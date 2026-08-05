package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service.HEADClientService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEADFileStorageService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADFileClientRequest;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadClientRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients/files/v1")
public class HEADClientToFilesController {


    @Autowired
    private HEADFileStorageService headFileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadClient(@ModelAttribute HEADUploadClientRequest headUploadClientRequest) throws Exception {
        return headFileStorageService.uploadClientDoc(headUploadClientRequest);
    }

    @PostMapping("/files")
    public ResponseEntity<?> listClient(@RequestBody HEADFileClientRequest headFileClientRequest)  {
        return headFileStorageService.listClient(headFileClientRequest);
    }

}
