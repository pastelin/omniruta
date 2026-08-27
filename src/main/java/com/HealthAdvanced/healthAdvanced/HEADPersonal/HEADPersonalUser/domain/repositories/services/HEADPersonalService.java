package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADTokenModel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthSessionBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder.HEADAppStateBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpResendRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpStarRes;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.contracts.IHEADOtpService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADAcceptLegalDocumentService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADLegalAcceptanceWriterService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADUserOrPersonalRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.servicesMap.HEADPersonalUserMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADPersonalUserMapServiceInterface;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service
@Transactional
@RequiredArgsConstructor
public class HEADPersonalService implements HEADPersonalUserMapServiceInterface {

    private final HEADPersonalUserMapService mapper;
    private final HEADPersonalUserRepository userRepo;
    private final HEADStepCurrentPersonalInterface steps;
    private final HEADAuthSessionBuilder sessionBuilder; // NUEVO
    private final IHEADOtpService otpService;
    private final HEADAuthService authService;
    private final HEADAppNavigatorService navigator;
    private final IHEADSexUserRepository sexRepo;
    private final HEADAppStateBuilder headAppStateBuilder;
    private final HEADStepCatalogueRepository catRepo;
    private final HEADLegalAcceptanceWriterService legalAcceptanceWriterService;
    private final HttpServletRequest request;

    private static final String ROLE_REGISTER = HEADConstantsSecurity.REGISTER_PERSONAL;

    /** OTP: verifica y devuelve estado + token */
    @Override
    public HEADCodeSecurityResponse otpVerifyPersonal(HEADVerifyRequest otpReq) {
        var code = otpService.verify(otpReq);

        HEADPersonalUser user = "PHONE".equals(code.channel())
                ? userRepo.findBytelefono(code.identifier()).orElse(null)
                : userRepo.findByEmail(code.identifier()).orElse(null);

        HEADCodeSecurityResponse res;

        if (user == null) {
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
            var appState = navigator.resolveStateForUuid(user.getUidUser());

            var state = steps.statusStaff(user.getIdUser());
            res = mapper.securityCodeMap(user, new HEADCodeSecurityResponse(), state);

            var out = res.getHeadJwtUsersResponse();
            out.setStepCurrent(appState.stepStatus());
            out.setHeadAppStateDTO(appState);
            out.setAccessToken(null);
            out.setExpiresAt(0);
            String screenFlowCurrent = otpReq.isRecoveryPassword() ? "RESETPASSWORD" : "LOGIN";
            var stepCurrentScreen = catRepo.findByTypeFlowAndStepName(HEADStepCurrentPersonalService.STAFF, screenFlowCurrent).orElse(null);
            var stepCurrent = res.getHeadJwtUsersResponse().getStepCurrent().withChangeScreenFlow(
                    stepCurrentScreen != null ? stepCurrentScreen.getScreenFlow() : "PasswordLogin");
            res.getHeadJwtUsersResponse().setStepCurrent(stepCurrent);
        }

        res.setOk(code.ok());
        return res;
    }


    @Override
    public ResponseEntity<HEADOtpStarRes> otpStartPersonal(HEADOtpRequest req) {
        HEADPersonalUser personal = userRepo.findByEmail(req.identifier()).orElse(null);
        if (personal != null && personal.getAuthProvider() == HEADAuthProvider.GOOGLE) {
            throw new HEADBadRequestException("Esta cuenta está asociada a Google. Inicia sesión con Google.");
        }
        personal = userRepo.findBytelefono(req.identifier()).orElse(null);
        if (personal != null && personal.getAuthProvider() == HEADAuthProvider.GOOGLE) {
            throw new HEADBadRequestException("Esta cuenta está asociada a Google. Inicia sesión con Google.");
        }
        var out = otpService.start(req, HEADStepCurrentPersonalService.STAFF);
        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    /** Registro personal: crea user REGISTER_PERSONAL, marca primer sub-paso y devuelve token + estado */
    @Override
    public ResponseEntity<HEADStaffRegisterResponseDto> savePersonal(HEADUserOrPersonalRequestDto dto) {
        var sex = sexRepo.findById(dto.getSexClient()).orElseThrow(() -> new HEADBadRequestException("El tipo de genero es incorrecto"));
        var user = mapper.createPersonalUser(mapper.personalUserMapRequest(dto, ROLE_REGISTER,sex.getTypeSex()), sex);
        var dup = userRepo.findBytelefono(user.getTelefono()).orElse(null);
        if (dup == null) dup = userRepo.findByEmail(user.getEmail()).orElse(null);
        if (dup != null) {
            throw new HEADBadRequestException("Esta cuenta ya esta registrada, favor de iniciar sesión");
        }

        user.setAuthProvider(HEADAuthProvider.LOCAL);
        user.setUidUser(generatorUUID());
        var saved = userRepo.save(user);

        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        String platform = request.getHeader(HEADHeadersConstants.PLATFORM);
        String ipAddress = HEADAcceptLegalDocumentService.extractIpAddress(request);
        String userAgent = HEADAcceptLegalDocumentService.extractUserAgent(request);
        String appVersion = request.getHeader(HEADHeadersConstants.APP_VERSION);

        legalAcceptanceWriterService.registerAcceptancesForNewUser(
                HEADLegalUserType.STAFF,
                saved.getIdUser(),
                dto.getTermsDocumentId(),
                dto.getPrivacyDocumentId(),
                appVersion,
                dto.getLanguage(),
                platform,
                deviceId,
                ipAddress,
                userAgent
        );

        steps.staffCompleteSub(saved.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.PROFILE_PASS.name());

        var state = steps.statusStaff(saved.getIdUser());
        var resp = mapper.staffRegisterResponseDto(saved);
        var appState = headAppStateBuilder.buildForStaff(saved,state);
        resp.setStepCurrent(state);
        resp.setAppStateDTO(appState);
        resp.setIsSuccess(true);
        resp.setIsExistsStaff(false);
        resp.setMessageUser("El personal se registro correctamente");

        var session = sessionBuilder.buildUserJwtAndState(
                saved.getUidUser(), saved.getIdUser(), saved.getEmail(),
                saved.getNombre(), saved.getAPaterno(), saved.getAMaterno(),
                saved.getIsEnabled(), saved.getTelefono()
        );
        resp.setTokenSuccess(session.getAccessToken());
        resp.setExpiresAt(session.getExpiresAt());
        resp.setRefreshToken(session.getRefreshToken());
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<?> loginPersonal(HEADLoginPasswordRequest req) {
        var id = req.identifier().trim();
        var userOpt = switch (req.channel().toUpperCase()) {
            case "EMAIL" -> userRepo.findByEmail(id);
            case "PHONE" -> userRepo.findBytelefono(id.replaceAll("[^0-9+]", ""));
            default -> throw new HEADBadRequestException("Canal inválido");
        };

        var user = userOpt.orElseThrow(() -> new HEADBadRequestException("Usuario no encontrado"));
        if (!HEADCommonsUtils.isMatchesPasswords(req.password(), user.getPassword()))
            throw new HEADBadRequestException("Credenciales inválidas");

        var tokens = authService.login(user.getUidUser());
        var appState = navigator.resolveStateForUuid(user.getUidUser());

        var out = mapper.headJwtUsersMap(
                user,
                appState.stepStatus(),
                new HEADTokenModel(tokens.getAccessToken(), tokens.getAccessExpiresAt())
        );
        out.setHeadAppStateDTO(appState);
        out.setStepCurrent(appState.stepStatus());
        out.setRefreshToken(tokens.getRefreshToken());

        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    public ResponseEntity<HEADApiResponse<HEADSuccessResetPassword>> resetPasswordStaff(HEADResetPassword req) {
        var id = req.identifier().trim();

        var userOpt = switch (req.channel().toUpperCase()) {
            case "EMAIL" -> userRepo.findByEmail(id);
            case "PHONE" -> userRepo.findBytelefono(normalizePhone(id));
            default -> throw new HEADBadRequestException("Canal inválido");
        };

        var user = userOpt.orElseThrow(() -> new HEADBadRequestException("Usuario no encontrado"));

        user.setPassword(HEADCommonsUtils.setEncodeValue(req.newPassword()));
        userRepo.saveAndFlush(user);

        var appState = navigator.resolveStateForUuid(user.getUidUser());

        var result = mapper.staffResetPassword(appState.stepStatus());

        return ResponseEntity.ok(
                HEADApiResponse.ok(result, "Password reseteado correctamente")
        );
    }

    public ResponseEntity<HEADApiResponse<HEADOtpStarRes>> resendCode(HEADOtpResendRequest request) {
        var out = otpService.resend(request.txId());
        return ResponseEntity.ok(HEADApiResponse.ok(out));

    }

    private String normalizePhone(String raw) {
        return raw.replaceAll("[^0-9+]", "");
    }

}
