package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.enums.HEADTypeUser;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB.HEADAuthDevice;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB.HEADAuthRefreshToken;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.response.HEADLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories.HEADAuthDeviceRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories.HEADAuthRefreshTokenRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.HEADServiceAuthentications;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.implementations.HEADPresenceRedisStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class HEADAuthService {

    private final HEADAuthDeviceRepository deviceRepo;
    private final HEADAuthRefreshTokenRepository refreshRepo;
    private final HEADServiceAuthentications headAuthLoader;
    private final HEADJwtGenerator jwt; // usa tu generador existente (access=10-15min recomendado)
    private final HttpServletRequest request;
    private final HEADAppNavigatorService navigator;
    private final HEADPresenceRedisStore presenceRedisStore;
    // Config de expiraciones
    private static final Duration ACCESS_TTL   = Duration.ofMinutes(15); // solo informativa aquí
    private static final Duration REFRESH_IDLE = Duration.ofDays(30);
    private static final Duration REFRESH_MAX  = Duration.ofDays(90);

    private static LocalDateTime now() { return LocalDateTime.now(); }

    private static String randomRefresh() {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new HEADBusinessException(e.getMessage()); }
    }

    // ===== LOGIN =====
    @Transactional
    public HEADLoginResponse login(String uuidUser) {
        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        String platform = request.getHeader(HEADHeadersConstants.PLATFORM);
        String getUUID = uuidUser != null ? uuidUser : jwt.getUserNamePersonalUser();
        var getUser = headAuthLoader.loadUserOrClient(getUUID);

        deviceRepo.findByUserIdAndDeviceId(getUser.idUser(), deviceId)
                .orElseGet(() -> {
                    var d = new HEADAuthDevice();
                    d.setUserId(getUser.idUser());
                    d.setDeviceId(deviceId);
                    d.setPlatform(platform);
                    d.setCreatedAt(now());
                    d.setUuidUser(getUser.uuidUser());
                    return deviceRepo.save(d);
                });

        var iat = now();
        var expAbs = iat.plus(REFRESH_MAX);

        String refresh = randomRefresh();
        String hash = sha256(refresh);

        var t = new HEADAuthRefreshToken();
        t.setUserId(getUser.idUser());
        t.setDeviceId(deviceId);
        t.setTokenHash(hash);
        t.setIssuedAt(iat);
        t.setLastUsedAt(iat);
        t.setExpiresAt(expAbs);
        t.setUuidUser(getUser.uuidUser());
        refreshRepo.save(t);

        UserDetails ud = headAuthLoader.loadUserOrClientByUsername(getUser.uuidUser());
        var access = jwt.generateToken(ud,getUser.typeUser()); // tu generador ya mete "roles" sin ROLE_

        return new HEADLoginResponse(access.tokenAccess(), access.expiresAt(), refresh, null);
    }

    // ===== REFRESH =====
    @Transactional
    public HEADLoginResponse refresh(String providedRefresh) {
        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        String hash = sha256(providedRefresh);
        HEADAuthRefreshToken getUUID = refreshRepo.findByTokenHash(hash).orElse(null);
        if (getUUID == null) {
            throw new HEADBadRequestException("Invalid refresh token");
        }
        var getUser = headAuthLoader.loadUserOrClient(getUUID.getUuidUser());
        var opt = refreshRepo.findOne(getUser.idUser(), deviceId, hash);
        if (opt.isEmpty()) {
            refreshRepo.revokeAllByDevice(getUser.idUser(), deviceId);
            throw new HEADBadRequestException("Invalid refresh token");
        }
        var token = opt.get();
        if (Boolean.TRUE.equals(token.getRevoked())) throw new HEADBadRequestException("Revoked");
        var now = now();
        if (now.isAfter(token.getExpiresAt())) {
            refreshRepo.revokeAllByDevice(getUser.idUser(), deviceId);
            throw new HEADBadRequestException("Expired refresh");
        }
        if (token.getLastUsedAt() != null && token.getLastUsedAt().plus(REFRESH_IDLE).isBefore(now)) {
            refreshRepo.revokeAllByDevice(getUser.idUser(), deviceId);
            throw new HEADBadRequestException("Refresh idle timeout");
        }

        // Rotar
        token.setRevoked(true);
        refreshRepo.save(token);

        String newRefresh = randomRefresh();
        String newHash = sha256(newRefresh);
        var appState = navigator.resolveStateForUuid(getUUID.getUuidUser());

        var rotated = new HEADAuthRefreshToken();
        rotated.setUserId(getUser.idUser());
        rotated.setDeviceId(deviceId);
        rotated.setTokenHash(newHash);
        rotated.setIssuedAt(now);
        rotated.setLastUsedAt(now);
        rotated.setExpiresAt(token.getIssuedAt().plus(REFRESH_MAX)); // respeta tope absoluto
        rotated.setRotationParentId(token.getId());
        rotated.setUuidUser(getUser.uuidUser());
        refreshRepo.save(rotated);

        UserDetails ud = headAuthLoader.loadUserOrClientByUsername(getUser.uuidUser()); // roles actuales (ej. ACCESS_CLIENT tras pasos)
        var access = jwt.generateToken(ud, getUser.typeUser());

        return new HEADLoginResponse(access.tokenAccess(), access.expiresAt(), newRefresh,appState.stepStatus().next().screenFlow());
    }

    // ===== LOGOUT =====
    @Transactional
    public void logout() {
        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        String getUUID = jwt.getUserNamePersonalUser();
        if (getUUID == null) {
            throw new HEADBadRequestException("El usuario no existe. favor de ingresar correctamente tu información requerida");
        }
        var getUser = headAuthLoader.loadUserOrClient(getUUID);
        refreshRepo.revokeAllByDevice(getUser.idUser(), deviceId);
        presenceRedisStore.sessionsOf(getUUID);
        presenceRedisStore.sessionsOf(getUUID).forEach(presenceRedisStore::remove);
    }
}
