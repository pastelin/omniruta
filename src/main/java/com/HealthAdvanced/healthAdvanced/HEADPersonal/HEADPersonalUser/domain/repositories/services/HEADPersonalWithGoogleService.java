package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADGoogleAuthRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADGoogleOauthProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder.HEADAppStateBuilder;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.servicesMap.HEADPersonalUserMapService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional
public class HEADPersonalWithGoogleService {

    private final HEADPersonalUserRepository userRepo;
    private final HEADStepCurrentPersonalInterface steps;
    private final HEADAuthService authService;
    private final HEADFileAssetRepository repoAssets;
    private final HEADGoogleOauthProperties googleOauthProperties;
    private final HEADPersonalUserMapService mapper;
    private final HEADAppNavigatorService navigator;
    private final HEADAppStateBuilder headAppStateBuilder;

    private static final String ROLE_REGISTER = HEADConstantsSecurity.REGISTER_PERSONAL;

    public HEADStaffRegisterResponseDto registerWithGoogle(HEADGoogleAuthRequest googleAuthRequest) {
        try {
            GoogleIdToken.Payload payload = verifyGoogleToken(googleAuthRequest.idToken());

            String googleSub = payload.getSubject();
            String email = payload.getEmail();

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

            email = email.trim().toLowerCase();

            HEADPersonalUser user = userRepo.findByGoogleSub(googleSub).orElse(null);
            if (user == null) {
                user = userRepo.findByEmail(email).orElse(null);
            }

            boolean isNewUser = (user == null);

            if (isNewUser) {
                user = new HEADPersonalUser();
                user.setUidUser(generatorUUID());
                user.setEmail(email);
                user.setNombre(resolveNombre(givenName, fullName));
                user.setAPaterno(resolveApellidoPaterno(familyName));
                user.setAMaterno(resolveApellidoMaterno(familyName));
                user.setGoogleSub(googleSub);
                user.setAuthProvider(HEADAuthProvider.GOOGLE);
                user.setPassword(null);

                // Staff nuevo por Google: sigue onboarding normal de personal
                user.setRoles(ROLE_REGISTER);

                var saved = userRepo.save(user);
                savePhotoUrl(saved, picture);

                steps.staffCompleteSub(
                        saved.getIdUser(),
                        HEADStepCode.REGISTER.name(),
                        HEADSubStepCode.PROFILE_PASS.name()
                );

                var state = steps.statusStaff(saved.getIdUser());
                var appState = headAppStateBuilder.buildForStaff(saved, state);
                var token = authService.login(saved.getUidUser());

                var resp = mapper.staffRegisterResponseDto(saved);
                resp.setIsRegisterUser(true);
                resp.setIsExistsStaff(false);
                resp.setIsSuccess(true);
                resp.setMessageUser("El personal con Google se registró correctamente");
                resp.setStepCurrent(state);
                resp.setAppStateDTO(appState);
                resp.setTokenSuccess(token.getAccessToken());
                resp.setExpiresAt(token.getAccessExpiresAt());
                resp.setRefreshToken(token.getRefreshToken());

                return resp;
            }

            // Si ya existía con login normal y no quieres mezclar métodos, bloquea
            if (user.getAuthProvider() != null
                    && user.getAuthProvider() == HEADAuthProvider.LOCAL
                    && user.getPassword() != null
                    && !user.getPassword().isBlank()
                    && (user.getGoogleSub() == null || user.getGoogleSub().isBlank())) {
                throw new HEADBadRequestException(
                        "Este correo ya está registrado con acceso normal. Inicia sesión con correo y contraseña."
                );
            }

            // Vincular / actualizar datos Google
            user.setGoogleSub(googleSub);
            user.setAuthProvider(HEADAuthProvider.GOOGLE);
            user.setPassword(null);

            if (isBlank(user.getNombre())) {
                user.setNombre(resolveNombre(givenName, fullName));
            }
            if (isBlank(user.getAPaterno())) {
                user.setAPaterno(resolveApellidoPaterno(familyName));
            }
            if (isBlank(user.getAMaterno())) {
                user.setAMaterno(resolveApellidoMaterno(familyName));
            }

            // Si por algún motivo no trae rol, lo dejas en registro
            if (isBlank(user.getRoles())) {
                user.setRoles(ROLE_REGISTER);
            }

            var saved = userRepo.save(user);
            savePhotoUrl(saved, picture);

            var token = authService.login(saved.getUidUser());
            var appState = navigator.resolveStateForUuid(saved.getUidUser());

            var resp = mapper.staffRegisterResponseDto(saved);
            resp.setIsRegisterUser(false);
            resp.setIsExistsStaff(true);
            resp.setIsSuccess(true);
            resp.setMessageUser("Inicio de sesión con Google correcto");
            resp.setStepCurrent(appState.stepStatus());
            resp.setAppStateDTO(appState);
            resp.setTokenSuccess(token.getAccessToken());
            resp.setExpiresAt(token.getAccessExpiresAt());
            resp.setRefreshToken(token.getRefreshToken());

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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void savePhotoUrl(HEADPersonalUser user, String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return;

        var avatar = repoAssets.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                HEADOwnerType.STAFF,
                user.getIdUser(),
                HEADCategory.AVATAR
        ).orElse(null);

        if (avatar != null) {
            return;
        }

        HEADFileAsset a = new HEADFileAsset();
        a.setOwnerType(HEADOwnerType.STAFF); // o HEADOwnerType.PERSONAL
        a.setOwnerId(user.getIdUser());
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

    private String generatorUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}