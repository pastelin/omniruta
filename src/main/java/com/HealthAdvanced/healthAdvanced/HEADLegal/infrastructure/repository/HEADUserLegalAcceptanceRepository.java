package com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository;


import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADUserLegalAcceptance;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HEADUserLegalAcceptanceRepository extends JpaRepository<HEADUserLegalAcceptance, Long> {

    @Query("""
        select count(a) > 0
        from HEADUserLegalAcceptance a
        where a.userType = :userType
          and a.userId = :userId
          and a.legalDocument.documentType = :documentType
          and a.legalDocument.version = :version
          and a.accepted = true
    """)
    boolean existsAcceptedDocumentVersion(
            @Param("userType") HEADLegalUserType userType,
            @Param("userId") Long userId,
            @Param("documentType") HEADLegalDocumentType documentType,
            @Param("version") String version
    );

    @Query("""
        select count(a) > 0
        from HEADUserLegalAcceptance a
        where a.userType = :userType
          and a.userId = :userId
          and a.legalDocument.id = :legalDocumentId
          and a.accepted = true
    """)
    boolean existsAcceptedLegalDocument(
            @Param("userType") HEADLegalUserType userType,
            @Param("userId") Long userId,
            @Param("legalDocumentId") Long legalDocumentId
    );
}