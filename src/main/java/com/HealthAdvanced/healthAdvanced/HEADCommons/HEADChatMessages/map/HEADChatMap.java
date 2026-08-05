package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.map;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADChatSendMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADChatFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HEADChatMap {

    private final HEADFileAssetRepository headFileAssetRepository;

    public HEADChatMessageDto toDto(HEADChatMessage m, String urlAvatarRecipient, String urlAvatarSender, String callerUuid) {
        Long jobId = (m.getJob() != null) ? m.getJob().getId() : null;

        Long fileId = null;
        String fileTitle = null;
        String fileContentType = null;
        Long fileSizeBytes = null;
        String urlFile = null;

        if (m.getFileAsset() != null) {
            fileId = m.getFileAsset().getId();
            fileTitle = m.getFileAsset().getTitle();
            fileContentType = m.getFileAsset().getContentType();
            fileSizeBytes = m.getFileAsset().getSizeBytes();
            urlFile = m.getFileAsset().getUrl();
        }

        return new HEADChatMessageDto(
                m.getId(),
                m.getConversationId(),
                jobId,
                m.getSenderUuid(),
                m.getSenderType(),            // 👈
                m.getRecipientUuid(),
                m.getRecipientType(),         // 👈
                m.getContent(),
                m.getType(),
                fileId,
                fileTitle,
                fileContentType,
                fileSizeBytes,
                urlFile,
                m.getStatus(),
                m.getCreatedAt(),
                m.getDeliveredAt(),
                m.getReadAt(),
                urlAvatarRecipient,
                urlAvatarSender,
                Objects.equals(callerUuid, m.getSenderUuid())
        );
    }

    public @NotNull HEADChatMessage newChatMessage(
            HEADChatSendMessageRequest req,
            HEADJob job,
            String senderUuid,
            String conversationId
    ) {
        HEADChatMessage message = new HEADChatMessage();
        message.setConversationId(conversationId);
        message.setJob(job);
        message.setSenderUuid(senderUuid);
        message.setRecipientUuid(req.recipientUuid());
        message.setContent(req.content());
        message.setType(req.type());
        message.setStatus(HEADChatMessageStatus.SENT);
        return message;
    }

    public HEADChatFileUploadResponse toResponseChatFile(HEADFileAsset headFileAsset) {
        return new HEADChatFileUploadResponse(
                headFileAsset.getId(),
                headFileAsset.getTitle(),
                headFileAsset.getContentType(),
                headFileAsset.getSizeBytes(),
                headFileAsset.getUrl(),
                null,                               // thumbnailUrl si luego lo tienes
                headFileAsset.getContentLength()
        );
    }
}

