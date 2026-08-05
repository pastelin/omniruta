package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationUiType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "head_notification_inbox",
        indexes = {
                @Index(name="idx_inbox_user_created", columnList="user_uuid,created_at"),
                @Index(name="idx_inbox_user_unread", columnList="user_uuid,is_read,created_at"),
                @Index(name="idx_inbox_dedupe", columnList="user_uuid,dedupe_key")
        }
)
public class HEADNotificationInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_uuid", nullable = false, length = 64)
    private String userUuid;

    @Enumerated(EnumType.STRING)
    @Column(name="ui_type", nullable = false, length = 20)
    private HEADNotificationUiType uiType;  // APPOINTMENT / REMINDER / INFO / PROMOTION

    @Enumerated(EnumType.STRING)
    @Column(name="event_type", nullable = false, length = 40)
    private HEADNotificationType eventType; // tu enum existente (SCHEDULE, etc.)

    @Column(name="title", nullable = false, length = 160)
    private String title;

    @Column(name="message", nullable = false, length = 700)
    private String message;

    @Column(name="icon", length = 50)
    private String icon; // emoji o url

    @Column(name="dedupe_key", length = 120)
    private String dedupeKey; // para evitar spam

    @Lob
    @Column(name="data_json", columnDefinition = "LONGTEXT")
    private String dataJson; // params (jobId, doseId, etc)

    @Column(name="is_read", nullable = false)
    private boolean isRead = false;

    @Column(name="read_at")
    private Instant readAt;

    @Column(name="deleted", nullable = false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private Instant deletedAt;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public void markRead() { this.isRead = true; this.readAt = Instant.now(); }
    public void softDelete() { this.deleted = true; this.deletedAt = Instant.now(); }
}