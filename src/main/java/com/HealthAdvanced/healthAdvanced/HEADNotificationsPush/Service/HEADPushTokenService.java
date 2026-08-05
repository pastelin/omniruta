package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADPushTokenEntity;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADPushPlatform;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository.HEADPushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADPushTokenService {

    private final HEADPushTokenRepository repo;

    @Transactional
    public void registerToken(String userUuid, HEADPushPlatform platform, String fcmToken) {

        repo.deactivateOtherTokens(userUuid, platform, fcmToken);

        var ent = repo.findByUserUuidAndPlatformAndFcmToken(userUuid, platform, fcmToken)
                .map(e -> {
                    e.setActive(true);
                    return e;
                })
                .orElseGet(() -> {
                    var e = new HEADPushTokenEntity();
                    e.setUserUuid(userUuid);
                    e.setPlatform(platform);
                    e.setFcmToken(fcmToken);
                    e.setActive(true);
                    return e;
                });

        repo.save(ent);
    }

    @Transactional(readOnly = true)
    public List<String> findTokensForUser(String userUuid) {
        return repo.findActiveTokensOnly(userUuid);
    }

    @Transactional
    public void deactivateToken(String fcmToken) {
        repo.deactivateByToken(fcmToken);
    }
}
