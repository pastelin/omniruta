package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADPushPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(
        name = "head_push_token",
        indexes = {
                @Index(name = "idx_push_token_user", columnList = "userUuid")
        }
)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class HEADPushTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userUuid;

    @Enumerated(EnumType.STRING)
    private HEADPushPlatform platform;

    @Column(nullable = false, length = 2048)
    private String fcmToken;

    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
