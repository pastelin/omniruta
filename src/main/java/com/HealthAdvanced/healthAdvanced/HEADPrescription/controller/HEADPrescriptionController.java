package com.HealthAdvanced.healthAdvanced.HEADPrescription.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADMedicationFormItem;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPdfResult;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.service.HEADClientPrescriptionPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/client/prescriptions")
@RequiredArgsConstructor
public class HEADPrescriptionController {

    private final HEADClientPrescriptionPdfService clientPdfService;

    @GetMapping(value = "/job/{jobId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdfByJobId(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "inline") String disposition // inline | attachment
    ) {;

        byte[] bytes = clientPdfService.buildClientPdfByJobId(jobId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receta-job-" + jobId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
