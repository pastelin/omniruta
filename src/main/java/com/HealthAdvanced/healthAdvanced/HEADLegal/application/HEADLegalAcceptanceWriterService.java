package com.HealthAdvanced.healthAdvanced.HEADLegal.application;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADLegalDocument;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADUserLegalAcceptance;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADLegalDocumentRepository;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADUserLegalAcceptanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADLegalAcceptanceWriterService {

    private final HEADLegalDocumentRepository legalDocumentRepository;
    private final HEADUserLegalAcceptanceRepository legalAcceptanceRepository;

    @Transactional
    public void registerAcceptancesForNewUser(
            HEADLegalUserType userType,
            Long userId,
            Long termsDocumentId,
            Long privacyDocumentId,
            String appVersion,
            String language,
            String platform,
            String deviceId,
            String ipAddress,
            String userAgent
    ) {
        HEADLegalDocument termsDocument = legalDocumentRepository.findByIdAndIsActiveTrue(termsDocumentId)
                .orElseThrow(() -> new HEADBadRequestException("Términos y condiciones no encontrados"));

        HEADLegalDocument privacyDocument = legalDocumentRepository.findByIdAndIsActiveTrue(privacyDocumentId)
                .orElseThrow(() -> new HEADBadRequestException("Aviso de privacidad no encontrado"));

        validateDocument(termsDocument, userType, HEADLegalDocumentType.TERMS);
        validateDocument(privacyDocument, userType, HEADLegalDocumentType.PRIVACY);

        legalAcceptanceRepository.save(buildAcceptance(
                userType,
                userId,
                termsDocument,
                appVersion,
                language,
                platform,
                deviceId,
                ipAddress,
                userAgent
        ));

        legalAcceptanceRepository.save(buildAcceptance(
                userType,
                userId,
                privacyDocument,
                appVersion,
                language,
                platform,
                deviceId,
                ipAddress,
                userAgent
        ));
    }

    private void validateDocument(
            HEADLegalDocument document,
            HEADLegalUserType expectedUserType,
            HEADLegalDocumentType expectedDocumentType
    ) {
        if (document.getUserType() != expectedUserType) {
            throw new HEADBadRequestException("El documento no corresponde al tipo de usuario");
        }

        if (document.getDocumentType() != expectedDocumentType) {
            throw new HEADBadRequestException("El tipo de documento legal no corresponde");
        }
    }

    private HEADUserLegalAcceptance buildAcceptance(
            HEADLegalUserType userType,
            Long userId,
            HEADLegalDocument document,
            String appVersion,
            String language,
            String platform,
            String deviceId,
            String ipAddress,
            String userAgent
    ) {
        HEADUserLegalAcceptance acceptance = new HEADUserLegalAcceptance();
        acceptance.setUserType(userType);
        acceptance.setUserId(userId);
        acceptance.setLegalDocument(document);
        acceptance.setAccepted(true);
        acceptance.setAppVersion(appVersion);
        acceptance.setLanguage(language);
        acceptance.setPlatform(platform);
        acceptance.setDeviceId(deviceId);
        acceptance.setIpAddress(ipAddress);
        acceptance.setUserAgent(userAgent);
        return acceptance;
    }
}