package com.HealthAdvanced.healthAdvanced.HEADLegal.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.request.HEADAcceptLegalDocumentRequest;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalAcceptanceResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalAcceptanceStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalDocumentResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADAcceptLegalDocumentService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADGetActiveLegalDocumentService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADGetLegalAcceptanceStatusService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/legal/staff")
public class HEADLegalStaffController {

    private final HEADAcceptLegalDocumentService headAcceptLegalDocumentService;
    private final HEADGetLegalAcceptanceStatusService legalAcceptanceStatusService;


    @PostMapping("/accept/document")
    public ResponseEntity<HEADApiResponse<HEADLegalAcceptanceResponse>> acceptDocument(
            @RequestBody HEADAcceptLegalDocumentRequest request
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(headAcceptLegalDocumentService.acceptForStaff(request)));
    }

    @GetMapping("/status/document")
    public ResponseEntity<HEADApiResponse<HEADLegalAcceptanceStatusResponse>> getStatusDocument() {
        return ResponseEntity.ok(HEADApiResponse.ok(legalAcceptanceStatusService.getStaffStatus()));
    }
}
