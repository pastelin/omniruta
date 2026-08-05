package com.HealthAdvanced.healthAdvanced.HEADAdmin.controller;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts.HEADAdminStaffDocumentsService;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminStaffDocumentDetailResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class HEADAdminStaffDocumentsController {

    private final HEADAdminStaffDocumentsService headAdminStaffDocumentsService;

    @GetMapping("/{userId}/documents")
    public ResponseEntity<HEADApiResponse<List<HEADAdminStaffDocumentDetailResponse>>> getDocumentsByStaff(
            @PathVariable Long userId,
            @RequestParam(required = false) HEADDocumentStatus status,
            @RequestParam(required = false) Long occProfileId
    ) {
        List<HEADAdminStaffDocumentDetailResponse> response =
                headAdminStaffDocumentsService.getDocumentsByStaff(userId, status, occProfileId);

        return ResponseEntity.ok(
                HEADApiResponse.ok(response,"Documentos obtenidos correctamente")
        );
    }
}