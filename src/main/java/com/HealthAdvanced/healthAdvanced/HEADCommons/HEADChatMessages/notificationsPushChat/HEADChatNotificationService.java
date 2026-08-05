package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADClientStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service.HEADChatProfileService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADChatNotificationService {

    private static final Locale LOCALE_MX = Locale.forLanguageTag("es-MX");

    private final HEADNotificationSender notificationSender;
    private final HEADChatProfileService chatProfileService;
    private final HEADClientStateStore clientStateStore;
    private final HEADStaffStateStore staffStateStore;

    public void notifyNewChatMessage(HEADChatMessageDto dto) {
        String recipientUuid = dto.recipientUuid();
        HEADChatParticipantType recipientType = dto.recipientType();
        String senderUuid = dto.senderUuid();

        if (recipientUuid == null || recipientType == null) return;

        if (!shouldSendPush(recipientUuid, recipientType)) {
            log.info("[CHAT-PUSH] skip appActive recipientUuid={} recipientType={}", recipientUuid, recipientType);
            return;
        }

        var senderProfile = chatProfileService.getProfile(senderUuid);
        String senderName = senderProfile != null && senderProfile.displayName() != null && !senderProfile.displayName().isBlank()
                ? senderProfile.displayName()
                : "Nuevo mensaje";

        String snippet = buildSnippet(dto);

        log.info("[CHAT-PUSH] senderUuid={} recipientUuid={} recipientType={} conversationId={}",
                senderUuid, recipientUuid, recipientType, dto.conversationId());

        String templateCode = switch (recipientType) {
            case CLIENT -> HEADNotificationTemplates.CHAT_NEW_MESSAGE_CLIENT;
            case STAFF  -> HEADNotificationTemplates.CHAT_NEW_MESSAGE_STAFF;
            default     -> HEADNotificationTemplates.CHAT_NEW_MESSAGE_CLIENT;
        };

        Map<String, Object> params = new HashMap<>();
        params.put("senderName", senderName);
        params.put("snippet", snippet);

        params.put("conversationId", dto.conversationId());
        params.put("jobId", dto.jobId());
        params.put("userType", recipientType.name());
        params.put("notificationDomain", "CHAT");

        params.put("collapseKey", "CHAT_" + dto.conversationId());
        params.put("tag", "CHAT_" + dto.conversationId());
        params.put("ttlSeconds", 3600);
        params.put("androidPriority", "HIGH");
        params.put("channelId", "chat_messages");
        params.put("deeplink", "head://chat/" + dto.conversationId());

        var command = new HEADNotificationCommand(
                recipientUuid,
                HEADNotificationType.CHAT_MESSAGE,
                templateCode,
                params,
                LOCALE_MX
        );

        notificationSender.send(command);
    }

    private boolean shouldSendPush(String userUuid, HEADChatParticipantType userType) {
        return switch (userType) {
            case CLIENT -> {
                var st = clientStateStore.get(userUuid);
                yield st == null || !Boolean.TRUE.equals(st.isAppActive());
            }
            case STAFF -> {
                var st = staffStateStore.get(userUuid);
                yield st == null || !Boolean.TRUE.equals(st.isAppActive());
            }
            default -> true;
        };
    }

    private String buildSnippet(HEADChatMessageDto dto) {
        return switch (dto.type()) {
            case IMAGE -> "📷 Imagen";
            case FILE  -> dto.fileTitle() != null ? "📎 " + dto.fileTitle() : "📎 Archivo";
            default    -> dto.content() != null && dto.content().length() > 60
                    ? dto.content().substring(0, 57) + "..."
                    : (dto.content() != null ? dto.content() : "Nuevo mensaje");
        };
    }
}