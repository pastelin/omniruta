package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessRegisterDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADClientRegisterRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.mapping.HEADClientLoginMapping;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service.HEADStepCurrentClientInterface;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthSessionBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADErrorCommonsSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder.HEADAppStateBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpResendRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpStarRes;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.contracts.IHEADOtpService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADAcceptLegalDocumentService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADLegalAcceptanceWriterService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service
@RequiredArgsConstructor
public class HEADClientService {

    private final HEADClientsRepository clientsRepo;
    private final IHEADSexUserRepository sexRepo;
    private final HEADClientLoginMapping mapper;
    private final HEADStepCurrentClientInterface steps;          // ya implementado
    private final HEADAuthSessionBuilder sessionBuilder;
    private final IHEADOtpService iheadOtpService;// NUEVO
    private final HEADAppStateBuilder headAppStateBuilder;
    private final HEADAuthService authService;
    private final HEADAppNavigatorService navigator;
    private final HEADJwtGenerator headJwtGenerator;
    private final HEADStepCatalogueRepository catRepo;
    private final HEADLegalAcceptanceWriterService legalAcceptanceWriterService;
    private final HttpServletRequest request;

    private static final String ROLE_REGISTER = HEADConstantsSecurity.REGISTER_CLIENT;

    public ResponseEntity<HEADClientRegisterResponseDto> registerClient(HEADClientRegisterRequestDto req) {
        var resp = new HEADClientRegisterResponseDto();

        var sex = sexRepo.findById(req.getSexClient()).orElse(new HEADSexUser());
        var newClient = mapper.clientRegisterMap(req);

        // deduplicación por email o teléfono
        var exists = clientsRepo.findByEmail(newClient.getEmail()).orElse(null);
        if (exists == null) exists = clientsRepo.findByTelefono(newClient.getTelefono()).orElse(null);

        if (exists != null) {
           throw new HEADBadRequestException("Esta cuenta ya esta registrada, favor de iniciar sesión");
        }

        // Nuevo cliente
        newClient.setIdSexUser(sex);
        newClient.setUuIdUser(generatorUUID());
        newClient.setRoles(ROLE_REGISTER); // REGISTER_CLIENT
        newClient.setAuthProvider(HEADAuthProvider.LOCAL);
        var saved = clientsRepo.save(newClient);

        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        String platform = request.getHeader(HEADHeadersConstants.PLATFORM);
        String ipAddress = HEADAcceptLegalDocumentService.extractIpAddress(request);
        String userAgent = HEADAcceptLegalDocumentService.extractUserAgent(request);
        String appVersion = request.getHeader(HEADHeadersConstants.APP_VERSION);

        legalAcceptanceWriterService.registerAcceptancesForNewUser(
                HEADLegalUserType.CLIENT,
                newClient.getIdUser(),
                req.getTermsDocumentId(),
                req.getPrivacyDocumentId(),
                appVersion,
                req.getLanguage(),
                platform,
                deviceId,
                ipAddress,
                userAgent
        );

        // Marca el primer sub-paso si corresponde (como ya hacías)
        steps.clientCompleteSub(saved.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.PROFILE_PASS.name());
        var dto = mapper.clientResponseDto(saved);
        resp.setIsSuccess(true);
        resp.setIsExistsClient(false);
        resp.setIsRegisterUser(true);
        resp.setDataClient(dto);
        resp.setMessageUser("El usuario se registró correctamente");

        // Adjunta token + estado
        sessionBuilder.attachTokenAndState(resp, saved.getUuIdUser());
        var stepStatus = steps.statusClient(saved.getIdUser());
        var appState = headAppStateBuilder.buildForClient(saved /* o exists */, stepStatus);
        resp.setStepCurrent(stepStatus);   // para compatibilidad
// si quieres exponer appState completo:
        resp.setAppStateDTO(appState);
        return ResponseEntity.ok(resp);
    }

    /** OTP: verifica y devuelve estado + token */
    public HEADCodeSecurityResponse otpVerifyClient(HEADVerifyRequest otpReq) {
        var code = iheadOtpService.verify(otpReq);

        // Buscar por email o teléfono según lo que devolvió tu OTP service
        HEADClients client = clientsRepo.findByEmail(code.identifier()).orElse(null);
        if (client == null) client = clientsRepo.findByTelefono(code.identifier()).orElse(null);

        HEADCodeSecurityResponse res;

        if (client == null) {
            // Aún no existe usuario: no hay token; solo navegación al siguiente subpaso
            String navigateSub = code.isVerified()
                    ? HEADSubStepCode.PROFILE_PASS.name()
                    : ("PHONE".equals(code.channel())
                    ? HEADSubStepCode.EMAIL_VERIFY.name()
                    : HEADSubStepCode.PHONE_VERIFY.name());

            var parentStep = code.isVerified() ? HEADStepCode.REGISTER.name()
                    : HEADStepCode.PRE_REGISTER.name();

            var nextSub = steps.getStepSubNext(parentStep, navigateSub);

            var next = new HEADStatusResponseDTO(
                    false,
                    new HEADNextDTO(true, parentStep, nextSub.getSubStepName(), nextSub.getScreenFlow()),
                    null
            );

            var userJwt = new HEADJwtUsersResponse();
            userJwt.setStepCurrent(next);

            res = new HEADCodeSecurityResponse();
            res.setHeadJwtUsersResponse(userJwt);
        } else {

            var appState = navigator.resolveStateForUuid(client.getUuIdUser());
            res = mapper.securityCodeMap(client, new HEADCodeSecurityResponse());
            var out = res.getHeadJwtUsersResponse();
            out.setAccessToken(null);
            out.setExpiresAt(0);

            out.setStepCurrent(appState.stepStatus());
            out.setHeadAppStateDTO(appState);
            out.setIsExistsPersonal(true);
            String screenFlowCurrent = otpReq.isRecoveryPassword() ? "RESETPASSWORD" : "LOGIN";
            var stepCurrentScreen = catRepo.findByTypeFlowAndStepName(steps.CLIENT, screenFlowCurrent).orElse(null);
            var stepCurrent = res.getHeadJwtUsersResponse().getStepCurrent().withChangeScreenFlow(
                    stepCurrentScreen != null ? stepCurrentScreen.getScreenFlow() : "PasswordLogin");
            res.getHeadJwtUsersResponse().setStepCurrent(stepCurrent);
        }
        res.setOk(code.ok());
        return res;
    }

    public ResponseEntity<HEADApiResponse<HEADOtpStarRes>> resendCode(HEADOtpResendRequest request) {
        var out = iheadOtpService.resend(request.txId());
        return ResponseEntity.ok(HEADApiResponse.ok(out));

    }

    /** OTP start (sin cambios) */
    public ResponseEntity<HEADOtpStarRes> otpStartClient(HEADOtpRequest req) {
        HEADClients personal = clientsRepo.findByEmail(req.identifier()).orElse(null);
        if (personal != null && personal.getAuthProvider() == HEADAuthProvider.GOOGLE) {
            throw new HEADBadRequestException("Esta cuenta está asociada a Google. Inicia sesión con Google.");
        }
        personal = clientsRepo.findByTelefono(req.identifier()).orElse(null);
        if (personal != null && personal.getAuthProvider() == HEADAuthProvider.GOOGLE) {
            throw new HEADBadRequestException("Esta cuenta está asociada a Google. Inicia sesión con Google.");
        }
        var out = iheadOtpService.start(req, steps.CLIENT);
        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    /** Para compatibilidad con tu flujo actual */
    public ResponseEntity<HEADCodeSecurityResponse> authenticationToken(HEADCodeSecurityResponse userRegister) {
        if (userRegister.getHeadJwtUsersResponse() != null) {
            var uuid = userRegister.getHeadJwtUsersResponse().getUidUser();
            var client = clientsRepo.findByUuIdUser(uuid).orElse(null);
            if (client != null) {
                var session = sessionBuilder.buildUserJwtAndState(
                        client.getUuIdUser(), client.getIdUser(), client.getEmail(),
                        client.getNombre(), client.getAPaterno(), client.getAMaterno(),
                        client.getIsAccepted(), client.getTelefono()
                );
                userRegister.getHeadJwtUsersResponse().setAccessToken(session.getAccessToken());
                userRegister.getHeadJwtUsersResponse().setExpiresAt(session.getExpiresAt());
            }
        }
        return new ResponseEntity<>(userRegister, HttpStatus.OK);
    }

    public ResponseEntity<?> successRegister() {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        var client = clientsRepo.findByUuIdUser(getUUID).orElseThrow(() ->
                new HEADBadRequestException("No eres cliente, favor de ingresar tus datos correctamente"));

        var stepStatus = steps.statusClient(client.getIdUser());
        if (Objects.equals(stepStatus.next().subStepName(), HEADSubStepCode.SUCCESS_REGISTER.name())) {
            steps.clientCompleteSub(client.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.SUCCESS_REGISTER.name());
            var stepStatusNext = steps.statusClient(client.getIdUser());
            var success = new HEADSuccessRegisterDto();
            var tokens = authService.login(getUUID);
            success.setStepCurrent(stepStatusNext);
            success.setAccessToken(tokens.getAccessToken());
            success.setExpiresAt(tokens.getAccessExpiresAt());
            success.setRefreshToken(tokens.getRefreshToken());
            return new ResponseEntity<>(success, HttpStatus.OK);
        }

        throw new HEADBadRequestException("Te faltan pasos para terminar tu registro");
    }
}
