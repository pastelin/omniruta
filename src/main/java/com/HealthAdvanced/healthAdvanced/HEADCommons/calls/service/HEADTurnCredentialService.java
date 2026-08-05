package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADTurnProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADTurnCredentialsResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADTurnIceServerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@Service
public class HEADTurnCredentialService {

    private final HEADTurnProperties properties;
    private final HEADJwtGenerator jwt;

    public HEADTurnCredentialsResponse generateForSubject() {
        var getUserUUid = jwt.getUserNamePersonalUser();
        long expiresAt = Instant.now().getEpochSecond() + properties.getTtlSeconds();
        String username = expiresAt + ":" + getUserUUid;
        String credential = hmacSha1Base64(properties.getSecret(), username);

        List<HEADTurnIceServerResponse> iceServers = List.of(
                new HEADTurnIceServerResponse(
                        List.of("stun:%s:%d".formatted(properties.getHost(), properties.getPort())),
                        null,
                        null
                ),
                new HEADTurnIceServerResponse(
                        List.of(
                                "turn:%s:%d?transport=udp".formatted(properties.getHost(), properties.getPort()),
                                "turn:%s:%d?transport=tcp".formatted(properties.getHost(), properties.getPort()),
                                "turns:%s:%d?transport=tcp".formatted(properties.getHost(), properties.getTlsPort())
                        ),
                        username,
                        credential
                )
        );

        return new HEADTurnCredentialsResponse(
                username,
                credential,
                expiresAt,
                iceServers
        );
    }

    private String hmacSha1Base64(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Error generating TURN credentials", e);
        }
    }
}