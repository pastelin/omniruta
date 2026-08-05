package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Controllers;


import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEADFileStorageService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEHOFileUploadUtil;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.*;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADDocumentsCatalogueResponse;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADFileUploadErrorResponse;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADFilesUploadResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/staffs/files/v1")
public class HEHODocumentController  {

    @Autowired
    private HEADFileStorageService headFileStorageService;

    @Operation(summary = "Catalogo de Archivos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalogo de archivos con exito",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADDocumentsCatalogueResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Catalogo de archivos fallido",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADFileUploadErrorResponse.class))
                    })
    })

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStaff(@ModelAttribute HEADUploadStaffRequest headUploadStaffRequest) throws Exception {
        return headFileStorageService.uploadStaffDoc(headUploadStaffRequest);
    }

    @PostMapping("/files")
    public ResponseEntity<?> listStaff(@RequestBody HEADFileStaffRequest headFileStaffRequest) {
        return headFileStorageService.listStaff(headFileStaffRequest);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam String storageKey) {
        boolean deleted = headFileStorageService.deleteFile(storageKey);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Archivo eliminado correctamente"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontró el archivo o no se pudo eliminar"));
        }
    }
}
