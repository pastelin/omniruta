package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADGoogleAuthRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.mapping.HEADClientLoginMapping;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service.HEADStepCurrentClientInterface;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthSessionBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADGoogleOauthProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder.HEADAppStateBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.contracts.IHEADOtpService;
import com.HealthAdvanced.healthAdvanced.HEADLegal.application.HEADLegalAcceptanceWriterService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service
@RequiredArgsConstructor
public class HEADClientWithGoogleService {

    private final HEADClientsRepository clientsRepo;
    private final HEADStepCurrentClientInterface steps;
    private final HEADAuthService authService;
    private final HEADFileAssetRepository repoAssets;
    private final HEADGoogleOauthProperties googleOauthProperties;

    private static final String ROLE_ACCESS = HEADConstantsSecurity.ACCESS_CLIENT;

    public HEADClientRegisterResponseDto registerWithGoogle(HEADGoogleAuthRequest googleAuthRequest) {
        try {
            GoogleIdToken.Payload payload = verifyGoogleToken(googleAuthRequest.idToken());

            String googleSub = payload.getSubject();          // sub
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();

            String fullName = (String) payload.get("name");
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");
            String picture = (String) payload.get("picture");

            if (googleSub == null || googleSub.isBlank()) {
                throw new HEADBadRequestException("No fue posible identificar la cuenta de Google");
            }

            if (email == null || email.isBlank()) {
                throw new HEADBadRequestException("No fue posible obtener el correo desde Google");
            }

            HEADClients client = clientsRepo.findByGoogleSub(googleSub).orElse(null);

            if (client == null) {
                client = clientsRepo.findByEmail(email.trim().toLowerCase()).orElse(null);
            }

            boolean isNewUser = (client == null);

            if (isNewUser) {
                client = new HEADClients();
                client.setUuIdUser(generatorUUID());
                client.setEmail(email.trim().toLowerCase());
                client.setNombre(resolveNombre(givenName, fullName));
                client.setAPaterno(resolveApellidoPaterno(familyName));
                client.setAMaterno(resolveApellidoMaterno(familyName));
                client.setGoogleSub(googleSub);
                client.setAuthProvider(HEADAuthProvider.GOOGLE);
                client.setPassword(null);
                // Decide si entra directo o sigue onboarding
                client.setRoles(ROLE_ACCESS);

                var saved = clientsRepo.save(client);

                savePhotoUrl(saved, picture);
                steps.clientCompleteSub(
                        saved.getIdUser(),
                        HEADStepCode.REGISTER.name(),
                        HEADSubStepCode.PROFILE_PASS.name()
                );

                steps.clientCompleteSub(client.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.SUCCESS_REGISTER.name());

                var token = authService.login(saved.getUuIdUser());

                var resp = new HEADClientRegisterResponseDto();
                resp.setIsRegisterUser(true);
                resp.setIsExistsClient(false);
                resp.setIsSuccess(true);
                resp.setMessageUser("El usuario con Google se registró correctamente");
                resp.setTokenSuccess(token.getAccessToken());
                resp.setExpiresAt(token.getAccessExpiresAt());
                resp.setRefreshToken(token.getRefreshToken());
                resp.setStepCurrent(steps.statusClient(saved.getIdUser()));
                return resp;
            }

            // Si ya existía con login normal y no quieres mezclar métodos, bloquea
            if (client.getAuthProvider() != null
                    && client.getAuthProvider() == HEADAuthProvider.LOCAL
                    && client.getPassword() != null
                    && !client.getPassword().isBlank()
                    && (client.getGoogleSub() == null || client.getGoogleSub().isBlank())) {
                throw new HEADBadRequestException(
                        "Este correo ya está registrado con acceso normal. Inicia sesión con correo y contraseña."
                );
            }

            // Vincular / actualizar datos Google
            client.setGoogleSub(googleSub);
            client.setAuthProvider(HEADAuthProvider.GOOGLE);
            client.setPassword(null);

            if (client.getNombre() == null || client.getNombre().isBlank()) {
                client.setNombre(resolveNombre(givenName, fullName));
            }
            if (client.getAPaterno() == null || client.getAPaterno().isBlank()) {
                client.setAPaterno(resolveApellidoPaterno(familyName));
            }
            if (client.getAMaterno() == null || client.getAMaterno().isBlank()) {
                client.setAMaterno(resolveApellidoMaterno(familyName));
            }

            // Si ya era cliente existente, normalmente ya debería poder entrar.
            if (!Objects.equals(client.getRoles(), ROLE_ACCESS)) {
                client.setRoles(ROLE_ACCESS);
            }

            var saved = clientsRepo.save(client);
            var token = authService.login(saved.getUuIdUser());
            savePhotoUrl(saved, picture);

            var resp = new HEADClientRegisterResponseDto();
            resp.setIsRegisterUser(false);
            resp.setIsExistsClient(true);
            resp.setIsSuccess(true);
            resp.setMessageUser("Inicio de sesión con Google correcto");
            resp.setTokenSuccess(token.getAccessToken());
            resp.setExpiresAt(token.getAccessExpiresAt());
            resp.setRefreshToken(token.getRefreshToken());
            resp.setStepCurrent(steps.statusClient(saved.getIdUser()));
            return resp;

        } catch (HEADBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new HEADBusinessException("No fue posible autenticar con Google: " + e.getMessage());
        }
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) throws Exception {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new HEADBadRequestException("El idToken de Google es obligatorio");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleOauthProperties.oauth().clientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new HEADBadRequestException("El token de Google no es válido");
        }

        return idToken.getPayload();
    }

    private String resolveNombre(String givenName, String fullName) {
        if (givenName != null && !givenName.isBlank()) return givenName;
        if (fullName != null && !fullName.isBlank()) return fullName;
        return "Usuario";
    }

    private String resolveApellidoPaterno(String familyName) {
        if (familyName == null || familyName.isBlank()) return "";
        String[] parts = familyName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    private String resolveApellidoMaterno(String familyName) {
        if (familyName == null || familyName.isBlank()) return "";
        String[] parts = familyName.trim().split("\\s+");
        return parts.length > 1 ? parts[1] : "";
    }

    private void savePhotoUrl(HEADClients client, String photoUrl) {
        var clientAvatar = repoAssets.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT,client.getIdUser(), HEADCategory.AVATAR).orElse(null);
        if (clientAvatar != null) {
            return;
        }
        HEADFileAsset a = new HEADFileAsset();
        a.setOwnerType(HEADOwnerType.CLIENT);
        a.setOwnerId(client.getIdUser());
        a.setCategory(HEADCategory.AVATAR);
        a.setVisibility(HEADVisibility.PRIVATE);
        a.setStorageKey(photoUrl);
        a.setUrl(photoUrl);
        a.setMimeType(null);
        a.setSizeBytes(0L);
        a.setActive(true);
        a.setSortOrder(0);
        a.setSubtitle(null);
        a.setScreenType(null);
        a.setDocumentCatalogue(-2);
        a.setTags(null);
        a.setContentType(null);
        a.setTitle(null);
        a.setContentLength(0L);
        repoAssets.save(a);
    }
}
