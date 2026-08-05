package com.HealthAdvanced.healthAdvanced.HEADLegal.application;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalAcceptanceStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADLegalDocumentRepository;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADUserLegalAcceptanceRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetLegalAcceptanceStatusService {

    private final HEADJwtGenerator jwt;
    private final HEADLegalDocumentRepository legalDocumentRepository;
    private final HEADUserLegalAcceptanceRepository legalAcceptanceRepository;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADClientsRepository clientsRepository;

    public HEADLegalAcceptanceStatusResponse getStaffStatus() {
        String staffUuid = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        return getStatus(HEADLegalUserType.STAFF, staff.getIdUser());
    }

    public HEADLegalAcceptanceStatusResponse getClientStatus() {
        String clientUuid = jwt.getUserNamePersonalUser();

        HEADClients client = clientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        return getStatus(HEADLegalUserType.CLIENT, client.getIdUser());
    }

    private HEADLegalAcceptanceStatusResponse getStatus(HEADLegalUserType userType, Long userId) {
        boolean termsAccepted = legalDocumentRepository
                .findFirstByUserTypeAndDocumentTypeAndIsActiveTrueOrderByPublishedAtDesc(userType, HEADLegalDocumentType.TERMS)
                .map(doc -> legalAcceptanceRepository.existsAcceptedDocumentVersion(
                        userType,
                        userId,
                        HEADLegalDocumentType.TERMS,
                        doc.getVersion()
                ))
                .orElse(false);

        boolean privacyAccepted = legalDocumentRepository
                .findFirstByUserTypeAndDocumentTypeAndIsActiveTrueOrderByPublishedAtDesc(userType, HEADLegalDocumentType.PRIVACY)
                .map(doc -> legalAcceptanceRepository.existsAcceptedDocumentVersion(
                        userType,
                        userId,
                        HEADLegalDocumentType.PRIVACY,
                        doc.getVersion()
                ))
                .orElse(false);

        return new HEADLegalAcceptanceStatusResponse(
                termsAccepted,
                privacyAccepted
        );
    }
}