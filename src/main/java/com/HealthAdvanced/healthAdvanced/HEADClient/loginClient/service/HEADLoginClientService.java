package com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.mapping.HEADClientLoginMapping;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.response.HEADUsersClientsResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service.HEADStepCurrentClientInterface;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADTokenModel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.HEADServiceAuthentications;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADAuthLevel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADHeadStep;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADRegStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.service.HEADSessionService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessagePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStepChecklistDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service
@RequiredArgsConstructor
public class HEADLoginClientService {

    private final HEADClientsRepository headClientsRepository;

    private final HEADJwtGenerator jwtService;

    private final HEADClientLoginMapping headClientLoginMapping;

    private final HEADStepCurrentClientInterface headStepCurrentClientInterface;

    private final HEADServiceAuthentications headServiceAuthentications;

    private final HEADAuthService authService;

    private final HEADAppNavigatorService navigator;

    private final PasswordEncoder passwordEncoder;

    private final HEADSessionService sessionService;

    // TEMPORAL: login de pruebas mientras se configura Firebase/registro real.
    // Apagado por defecto.
    @Value("${head.security.test-login.enabled:false}")
    private boolean testLoginEnabled;

    @Value("${head.security.test-login.identifier:test@test.com}")
    private String testLoginIdentifier;

    @Value("${head.security.test-login.password:Test1234!}")
    private String testLoginPassword;

    public ResponseEntity<?> loginClientAuth(HEADLoginPasswordRequest req) {
        var id = req.identifier().trim();

        HEADClients user;
        if (testLoginEnabled && testLoginIdentifier.equalsIgnoreCase(id) && testLoginPassword.equals(req.password())) {
            user = headClientsRepository.findByEmail(testLoginIdentifier).orElseGet(this::createTestClient);
        } else {
            var userOpt = switch (req.channel().toUpperCase()) {
                case "EMAIL" -> headClientsRepository.findByEmail(id);
                case "PHONE" -> headClientsRepository.findByTelefono(normalizePhone(id));
                default -> throw new HEADBadRequestException("Canal inválido");
            };

            user = userOpt.orElseThrow(() -> new HEADBadRequestException("Usuario no encontrado"));
            if (!passwordEncoder.matches(req.password(), user.getPassword())) {
                throw new HEADBadRequestException("Credenciales inválidas");
            }
        }

        sessionService.set(user.getUuIdUser(), HEADAuthLevel.PASSWORD_VERIFIED);

        var tokens = authService.login(user.getUuIdUser());

        var appState = navigator.resolveStateForUuid(user.getUuIdUser());

        // Si tu "registro completo" es equivalente a doneAll(), úsalo; si no, calcula
        // con tus reglas.
        var getStepCurrent = appState.stepStatus().checklist().stream()
                .filter(steps -> equalsEnumName(HEADHeadStep.REGISTER, steps.stepName())).findFirst().orElse(null);

        // 6) decidir el step a devolver considerando sesión por device + registro

        final var decidedStep = sessionService.decideStep(user.getUuIdUser(),
                safeEnum(HEADRegStatus.class, getStepCurrent.stepName(), HEADRegStatus.NONE));

        // 7) Busca en checklist el item que corresponde al decidedStep (puede no
        // existir)
        final var checklist = appState.stepStatus().checklist(); // List<StepItem>
        final var stepItemOpt = checklist.stream()
                .filter(s -> equalsEnumName(decidedStep, s.stepName()))
                .findFirst();

        // Si no está en checklist, arma un NextDTO "mínimo" con el decidedStep
        final var stepItem = stepItemOpt.orElse(null);
        final boolean doneFlag = (stepItem != null && Boolean.TRUE.equals(stepItem.done()));
        final String stepName = decidedStep.name(); // fallback al nombre del enum
        final String screenFlow = (stepItem != null && stepItem.screenFlow() != null)
                ? stepItem.screenFlow()
                : appState.goToScreen(); // o algún mapping a tu flow

        // 8) construir respuesta final
        final var out = headClientLoginMapping.clientLoginMapDto(
                user,
                appState.stepStatus(), // estado completo por compatibilidad con tu mapper
                new HEADTokenModel(tokens.getAccessToken(), tokens.getAccessExpiresAt()));
        out.setRefreshToken(tokens.getRefreshToken());
        out.setHeadAppStateDTO(appState);
        out.setStepCurrent(
                new HEADStatusResponseDTO(
                        appState.stepStatus().doneAll(),
                        new HEADNextDTO(doneFlag, stepName, stepName, screenFlow),
                        checklist // ya es lista, no re-uses de streams
                ));
        return ResponseEntity.ok(out);
    }

    private String normalizePhone(String raw) {
        return raw.replaceAll("[^0-9+]", "");
    }

    // TEMPORAL: crea el cliente de pruebas en el primer login si aún no existe
    private HEADClients createTestClient() {
        // var client = new HEADClients();
        // client.setNombre("Usuario");
        // client.setAPaterno("Prueba");
        // client.setEmail(testLoginIdentifier);
        // client.setTelefono("5210000000");
        // client.setPassword(passwordEncoder.encode(testLoginPassword));
        // client.setUuIdUser(generatorUUID());
        // client.setRoles(HEADConstantsSecurity.ACCESS_CLIENT);
        // client.setAuthProvider(HEADAuthProvider.LOCAL);
        // var saved = headClientsRepository.save(client);
        // headStepCurrentClientInterface.clientCompleteSub(saved.getIdUser(),
        // HEADStepCode.REGISTER.name(), HEADSubStepCode.PROFILE_PASS.name());
        // return saved;

        var client = new HEADClients();

        client.setNombre("Usuario");
        client.setAPaterno("Prueba");
        client.setEmail(testLoginIdentifier);
        client.setTelefono("5210000000");
        client.setPassword(passwordEncoder.encode(testLoginPassword));
        client.setUuIdUser(generatorUUID());
        client.setRoles(HEADConstantsSecurity.ACCESS_CLIENT);
        client.setAuthProvider(HEADAuthProvider.LOCAL);

        return headClientsRepository.save(client);
    }

    // Evita HEADBadRequestException si el nombre no coincide exactamente
    private static boolean equalsEnumName(Enum<?> e, String raw) {
        return raw != null && e.name().equalsIgnoreCase(raw.trim());
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> type, String raw, E fallback) {
        if (raw == null)
            return fallback;
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public ResponseEntity<HEADApiResponse<HEADSuccessResetPassword>> resetPasswordClient(HEADResetPassword req) {
        var id = req.identifier().trim();

        var userOpt = switch (req.channel().toUpperCase()) {
            case "EMAIL" -> headClientsRepository.findByEmail(id);
            case "PHONE" -> headClientsRepository.findByTelefono(normalizePhone(id));
            default -> throw new HEADBadRequestException("Canal inválido");
        };

        var user = userOpt.orElseThrow(() -> new HEADBadRequestException("Usuario no encontrado"));

        user.setPassword(HEADCommonsUtils.setEncodeValue(req.newPassword()));
        headClientsRepository.saveAndFlush(user);

        var appState = navigator.resolveStateForUuid(user.getUuIdUser());

        var result = headClientLoginMapping.clientResetPassword(appState.stepStatus());

        return ResponseEntity.ok(
                HEADApiResponse.ok(result, "Password reseteado correctamente"));
    }

}
