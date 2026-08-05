package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPdfResult;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class HEADClientPrescriptionPdfService {

    private final HEADPrescriptionJpaRepository prescriptionRepo;
    private final HEADPrescriptionPdfService pdfService;
    private final HEADJwtGenerator jwt;

    @Transactional(readOnly = true)
    public byte[] buildClientPdfByJobId(Long jobId) {
        String uuid = jwt.getUserNamePersonalUser();
        var rx = prescriptionRepo.findByJobIdAndClientUuidWithMeds(jobId, uuid)
                .orElseThrow(() -> new HEADBadRequestException( "Prescription not found"));

        byte[] pdf = pdfService.buildPdf(rx);

        return pdf;
    }
}
