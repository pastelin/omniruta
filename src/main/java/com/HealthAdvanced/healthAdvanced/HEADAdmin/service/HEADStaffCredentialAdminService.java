package com.HealthAdvanced.healthAdvanced.HEADAdmin.service;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADReviewLicenseCredentialRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADCredentialType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HEADStaffCredentialAdminService {

    private final HEADStaffCredentialRepository credentialRepo;
    private final HEADDocumentsRepository documentsRepo;
    private final HEADDocumentCatalogueRepository headDocumentCatalogueRepository;

    @Transactional
    public void reviewLicenseCredential(Long adminId, HEADReviewLicenseCredentialRequest req) {
        var cred = credentialRepo.findById(req.credentialId())
                .orElseThrow(() -> new HEADBadRequestException("Credential not found: " + req.credentialId()));

        if (req.licenseNo() != null && !req.licenseNo().trim().isEmpty()) {
            cred.setValue(req.licenseNo().trim());
        }

        cred.setStatus(req.status());
        cred.setReviewedByAdminId(adminId);
        cred.setReviewedAt(LocalDateTime.now());

        if (req.sourceDocumentId() != null) {
            var doc = documentsRepo.findById(req.sourceDocumentId())
                    .orElseThrow(() -> new HEADBadRequestException("Document not found: " + req.sourceDocumentId()));
            cred.setSourceDocument(doc);
        }

        cred.setUpdatedAt(LocalDateTime.now());
        credentialRepo.save(cred);
    }

    @Transactional(readOnly = true)
    public String getApprovedLicenseNo(Long staffUserId, Long occProfileIdOrNull) {
        return credentialRepo.findApprovedValueByStaffAndOccProfile(
                        staffUserId, occProfileIdOrNull, HEADCredentialType.LICENSE_NO
                )
                .or(() -> credentialRepo.findApprovedValueGlobal(staffUserId, HEADCredentialType.LICENSE_NO))
                .orElse(null);
    }

    private void syncCredentialIfCedula(Long userId, Integer docId,HEADDocumentStatus status, Long adminId) {
        HEADDocumentCatalogue catalogue = headDocumentCatalogueRepository.findById(docId).orElseThrow(() -> new HEADBadRequestException("Documento catálogo no válido: " + docId));
        if (docId == null ||  Objects.equals(catalogue.getNameDocument(), "CELULA")) return;

        // Buscamos la credencial global
        var credOpt = credentialRepo.findByStaffUser_IdUserAndOccupationProfileIsNullAndCredentialType(
                userId,
                HEADCredentialType.LICENSE_NO
        );

        credOpt.ifPresent(cred -> {
            cred.setStatus(status);
            cred.setReviewedByAdminId(adminId);
            cred.setReviewedAt(java.time.LocalDateTime.now());
            cred.setUpdatedAt(java.time.LocalDateTime.now());
            credentialRepo.save(cred);
        });
    }

    @Transactional(readOnly = true)
    public String resolveLicenseNoForJob(Long staffUserId, Long occProfileId) {
        return credentialRepo.findApprovedValueByStaffAndOccProfile(staffUserId, occProfileId, HEADCredentialType.LICENSE_NO)
                .or(() -> credentialRepo.findApprovedValueByStaffAndOccupationCode(
                        staffUserId, HEADCredentialType.LICENSE_NO, HEADOccupationCode.DOCTOR
                ).stream().findFirst())
                .orElse(null);
    }


}
