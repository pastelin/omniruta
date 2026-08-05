package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "head_chat_message",
        indexes = {
                @Index(name = "idx_conv_created", columnList = "conversationId, createdAt"),
                @Index(name = "idx_recipient_status", columnList = "recipientUuid, status")
        }
)
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"job", "fileAsset"})
public class HEADChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Id lógico de la conversación.
     * Ej: "userA|userB" o el uuid del job.
     */
    @Column(nullable = false, length = 100)
    private String conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_job",
            foreignKey = @ForeignKey(name = "fk_chat_message_job"),
            nullable = true
    )
    private HEADJob job;

    @Column(nullable = false, length = 60)
    private String senderUuid;      // uuIdUser del que envía

    @Column(nullable = false, length = 60)
    private String recipientUuid;   // uuIdUser del que recibe

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HEADChatMessageType type = HEADChatMessageType.TEXT;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HEADChatMessageStatus status = HEADChatMessageStatus.SENT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant deliveredAt;

    @Column
    private Instant readAt;

    // Por si quieres "borrar para mí"
    @Column(nullable = false)
    private boolean deletedBySender = false;

    @Column(nullable = false)
    private boolean deletedByRecipient = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HEADChatParticipantType senderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HEADChatParticipantType recipientType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_file_asset",
            foreignKey = @ForeignKey(name = "fk_chat_message_file_asset"),
            nullable = true
    )
    private HEADFileAsset fileAsset;
}
