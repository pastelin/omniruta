package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADPushTokenEntity;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADPushPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HEADPushTokenRepository extends JpaRepository<HEADPushTokenEntity, Long> {

    List<HEADPushTokenEntity> findByUserUuidAndActiveTrue(String userUuid);

    Optional<HEADPushTokenEntity> findByUserUuidAndPlatformAndFcmToken(String userUuid, HEADPushPlatform platform, String fcmToken);

    default List<String> findActiveTokensOnly(String userUuid) {
        return findByUserUuidAndActiveTrue(userUuid)
                .stream()
                .map(HEADPushTokenEntity::getFcmToken)
                .toList();
    }

    @Modifying
    @Query("""
           update HEADPushTokenEntity t
           set t.active = false
           where t.userUuid = :userUuid
             and t.platform = :platform
             and t.fcmToken <> :fcmToken
           """)
    void deactivateOtherTokens(String userUuid,
                               HEADPushPlatform platform,
                               String fcmToken);

    @Modifying
    @Query("""
           update HEADPushTokenEntity t
           set t.active = false
           where t.fcmToken = :fcmToken
           """)
    void deactivateByToken(String fcmToken);
}


