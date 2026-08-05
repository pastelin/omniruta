package com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository;

import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADLegalDocument;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HEADLegalDocumentRepository extends JpaRepository<HEADLegalDocument, Long> {

    Optional<HEADLegalDocument> findFirstByUserTypeAndDocumentTypeAndIsActiveTrueOrderByPublishedAtDesc(
            HEADLegalUserType userType,
            HEADLegalDocumentType documentType
    );

    Optional<HEADLegalDocument> findByIdAndIsActiveTrue(Long id);

    List<HEADLegalDocument> findAllByUserTypeAndIsActiveTrueOrderByPublishedAtDesc(
            HEADLegalUserType userType
    );
}