package com.HealthAdvanced.healthAdvanced.HEADLegal.application;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.request.HEADAcceptLegalDocumentRequest;
import com.HealthAdvanced.healthAdvanced.HEADLegal.api.response.HEADLegalAcceptanceResponse;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADLegalDocument;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity.HEADUserLegalAcceptance;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADLegalDocumentRepository;
import com.HealthAdvanced.healthAdvanced.HEADLegal.infrastructure.repository.HEADUserLegalAcceptanceRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADAcceptLegalDocumentService {

    private final HEADJwtGenerator jwt;
    private final HEADLegalDocumentRepository legalDocumentRepository;
    private final HEADUserLegalAcceptanceRepository legalAcceptanceRepository;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADClientsRepository clientsRepository;
    private final HttpServletRequest httpRequest;

    @Transactional
    public HEADLegalAcceptanceResponse acceptForStaff(
            HEADAcceptLegalDocumentRequest request
    ) {
        String staffUuid = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        return accept(
                HEADLegalUserType.STAFF,
                staff.getIdUser(),
                request
        );
    }

    @Transactional
    public HEADLegalAcceptanceResponse acceptForClient(
            HEADAcceptLegalDocumentRequest request
    ) {
        String clientUuid = jwt.getUserNamePersonalUser();

        HEADClients client = clientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        return accept(
                HEADLegalUserType.CLIENT,
                client.getIdUser(),
                request
        );
    }

    private HEADLegalAcceptanceResponse accept(
            HEADLegalUserType userType,
            Long userId,
            HEADAcceptLegalDocumentRequest request
    ) {
        HEADLegalDocument document = legalDocumentRepository.findByIdAndIsActiveTrue(request.legalDocumentId())
                .orElseThrow(() -> new HEADBadRequestException("Documento legal no encontrado"));

        if (document.getUserType() != userType) {
            throw new HEADBadRequestException("El documento legal no corresponde al tipo de usuario");
        }

        boolean alreadyAccepted = legalAcceptanceRepository.existsAcceptedLegalDocument(
                userType,
                userId,
                document.getId()
        );

        String deviceId = httpRequest.getHeader(HEADHeadersConstants.DEVICE_ID);
        String platform = httpRequest.getHeader(HEADHeadersConstants.PLATFORM);
        String appVersion = httpRequest.getHeader(HEADHeadersConstants.APP_VERSION);

        if (alreadyAccepted) {
            return new HEADLegalAcceptanceResponse(
                    document.getId(),
                    document.getVersion(),
                    null,
                    platform,
                    deviceId
            );
        }

        HEADUserLegalAcceptance acceptance = new HEADUserLegalAcceptance();
        acceptance.setUserType(userType);
        acceptance.setUserId(userId);
        acceptance.setLegalDocument(document);
        acceptance.setAccepted(true);
        acceptance.setAppVersion(safe(appVersion));
        acceptance.setLanguage(safe(request.language()));
        acceptance.setPlatform(safe(platform));
        acceptance.setDeviceId(safe(deviceId));
        acceptance.setIpAddress(extractIpAddress(httpRequest));
        acceptance.setUserAgent(extractUserAgent(httpRequest));

        acceptance = legalAcceptanceRepository.save(acceptance);

        return new HEADLegalAcceptanceResponse(
                document.getId(),
                document.getVersion(),
                acceptance.getAcceptedAt(),
                acceptance.getPlatform(),
                acceptance.getDeviceId()
        );
    }

    public static String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    public static String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.trim() : null;
    }

    private String safe(String value) {
        return value != null ? value.trim() : null;
    }
}