package com.HealthAdvanced.healthAdvanced.HEADAdmin.service;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts.HEADAdminStaffDocumentsService;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminStaffDocumentDetailResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocuments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADAdminStaffDocumentsServiceImpl implements HEADAdminStaffDocumentsService {

    private final HEADDocumentsRepository headDocumentsRepository;
    private final HEADPersonalUserRepository headPersonalUserRepository;

    @Override
    public List<HEADAdminStaffDocumentDetailResponse> getDocumentsByStaff(
            Long userId,
            HEADDocumentStatus status,
            Long occProfileId
    ) {
        if (!headPersonalUserRepository.existsById(userId)) {
            throw new HEADBadRequestException("Usuario inválido");
        }

        return headDocumentsRepository.findAdminDocumentDetailByUser(userId, status, occProfileId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HEADAdminStaffDocumentDetailResponse toResponse(HEADDocuments doc) {
        return HEADAdminStaffDocumentDetailResponse.builder()
                .idDocs(doc.getIdDocs())
                .documentCatalogueId(
                        doc.getIdDocument() != null ? doc.getIdDocument().getIdDocument() : null
                )
                .nombreArchivo(doc.getNombreArchivo())
                .extension(doc.getExtension())
                .url(doc.getUrl())
                .mimeType(doc.getMimeType())
                .sizeBytes(doc.getSizeBytes())
                .storageKey(doc.getStorageKey())
                .status(doc.getStatus())
                .reviewNotes(doc.getReviewNotes())
                .reviewedByAdminId(doc.getReviewedByAdminId())
                .uploadedAt(doc.getUploadedAt())
                .reviewedAt(doc.getReviewedAt())
                .occupationProfileId(
                        doc.getOccupationProfile() != null
                                ? doc.getOccupationProfile().getIdOccupationProfile()
                                : null
                )
                .build();
    }
}