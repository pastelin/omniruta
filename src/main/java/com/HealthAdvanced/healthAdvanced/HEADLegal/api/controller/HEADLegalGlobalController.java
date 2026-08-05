package com.HealthAdvanced.healthAdvanced.HEADLegal.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalDocumentResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADGetActiveLegalDocumentService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/legal/global")
public class HEADLegalGlobalController {

    private final HEADGetActiveLegalDocumentService activeLegalDocumentService;

    @GetMapping("/document/current")
    public ResponseEntity<HEADApiResponse<List<HEADLegalDocumentResponse>>> getDocumentActive(
            @RequestParam HEADLegalUserType legalUserType
    ){
        var getActiveDocument = activeLegalDocumentService.execute(legalUserType);
        return ResponseEntity.ok(HEADApiResponse.ok(getActiveDocument));
    }
}
