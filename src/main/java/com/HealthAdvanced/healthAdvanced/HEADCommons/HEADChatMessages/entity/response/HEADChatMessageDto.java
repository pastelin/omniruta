package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;

import java.time.Instant;

public record HEADChatMessageDto(
        Long id,
        String conversationId,
        Long jobId,
        String senderUuid,
        HEADChatParticipantType senderType,
        String recipientUuid,
        HEADChatParticipantType recipientType,
        String content,
        HEADChatMessageType type,
        Long fileAssetId,
        String fileTitle,
        String fileContentType,
        Long fileSizeBytes,
        String urlFile,
        HEADChatMessageStatus status,
        Instant createdAt,
        Instant deliveredAt,
        Instant readAt,
        String urlAvatarRecipient,
        String urlAvatarSender,
        Boolean isMe
) {
    public HEADChatMessageDto withIsMe(Boolean newIsMe) {
        return new HEADChatMessageDto(
                this.id,
                this.conversationId,
                this.jobId,
                this.senderUuid,
                this.senderType,
                this.recipientUuid,
                this.recipientType,
                this.content,
                this.type,
                this.fileAssetId,
                this.fileTitle,
                this.fileContentType,
                this.fileSizeBytes,
                this.urlFile,
                this.status,
                this.createdAt,
                this.deliveredAt,
                this.readAt,
                this.urlAvatarRecipient,
                this.urlAvatarSender,
                newIsMe
        );
    }
}
