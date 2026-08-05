package com.HealthAdvanced.healthAdvanced.HEADLegal.application;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalDocumentResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADLegalDocument;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADLegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetActiveLegalDocumentService {

    private final HEADLegalDocumentRepository legalDocumentRepository;

    public List<HEADLegalDocumentResponse> execute(
            HEADLegalUserType userType
    ) {
        var documents = legalDocumentRepository
                .findAllByUserTypeAndIsActiveTrueOrderByPublishedAtDesc(userType);

        return documents.stream().map(this::toResponse).toList();
    }

    private HEADLegalDocumentResponse toResponse(HEADLegalDocument document) {
        return new HEADLegalDocumentResponse(
                document.getId(),
                document.getUserType(),
                document.getDocumentType(),
                document.getTitle(),
                document.getVersion(),
                document.getContentUrl(),
                document.getContentText(),
                document.getLanguage()
        );
    }
}