package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventPublisher;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADChatPresenceUpdateSocketDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName.HEADChatWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.interfaces.HEADChatUnreadSummary;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName.HEADChatWsEvents.CHAT_PRESENCE_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADSocketChatEventPublisher implements HEADChatEventPublisher {

    private final HEADWsEmitter emitter;

    // ---------------------------------------------------------------------
    // Helpers internos
    // ---------------------------------------------------------------------

    private void emitToParticipant(String userUuid,
                                   HEADChatParticipantType type,
                                   String event,
                                   Object payload) {
        if (userUuid == null || userUuid.isBlank() || type == null) {
            return;
        }
        try {
            switch (type) {
                case STAFF -> emitter.toUser(userUuid, event, payload);
                case CLIENT -> emitter.emitToClient(userUuid, event, payload);
                case ADMIN, SYSTEM -> emitter.toUser(userUuid, event, payload);
                default -> emitter.toUser(userUuid, event, payload);
            }
        } catch (Exception ex) {
            log.error("[ChatEventPublisher] failed emit event={} userUuid={} type={} err={}",
                    event, userUuid, type, ex.toString(), ex);
        }
    }

    // ---------------------------------------------------------------------
    // Implementación de HEADChatEventPublisher
    // ---------------------------------------------------------------------

    @Override
    public void messageSent(HEADChatMessageDto dto) {
        final String event = HEADChatWsEvents.CHAT_MESSAGE;

        // 2) Si no hay job (chat directo), emitimos por usuario según tipo
        emitToParticipant(dto.senderUuid(), dto.senderType(), event, dto.withIsMe(true));
        emitToParticipant(dto.recipientUuid(), dto.recipientType(), event, dto.withIsMe(false));

    }

    @Override
    public void messagesHistory(String userUuid, HEADChatParticipantType type, HEADChatHistoryPageDto headChatHistoryPageDto) {
        final String event = HEADChatWsEvents.CHAT_HISTORY_RESPONSE;
        emitToParticipant(userUuid,type, event, headChatHistoryPageDto);
    }

    @Override
    public void messageStatusUpdated(HEADChatMessageStatusUpdateDto dto, HEADChatMessage message) {
        final String event = HEADChatWsEvents.CHAT_MESSAGE_STATUS_UPDATE;

        emitToParticipant(message.getSenderUuid(), message.getSenderType(), event, dto);
        emitToParticipant(message.getRecipientUuid(), message.getRecipientType(), event, dto);
    }

    @Override
    public void typingUpdated(HEADChatTypingUpdateDto dto, String uuidUser, HEADChatParticipantType type) {
        final String event = HEADChatWsEvents.CHAT_TYPING_UPDATE;

        emitToParticipant(uuidUser, type, event, dto);
    }

    @Override
    public void unreadSummaryUpdated(String userUuid,
                                     HEADChatParticipantType type,
                                     List<HEADChatUnreadSummary> summary) {
        final String event = HEADChatWsEvents.CHAT_UNREAD_SUMMARY;
        emitToParticipant(userUuid, type, event, summary);
    }

    @Override
    public void presenceUpdated(HEADChatPresenceDto presence) {
        var dto = new HEADChatPresenceUpdateSocketDto(
                presence.userUuid(),
                presence.userType(),
                presence.online(),
                presence.appActive(),
                presence.currentJobId(),
                presence.lastUpdatedAt(),
                presence.activeConversationId()
        );

        // ✅ manda al mismo usuario (para que su app se entere)
        emitToParticipant(presence.userUuid(), presence.userType(), CHAT_PRESENCE_UPDATED, dto);
    }

    @Override
    public void chatActive(String toUuid, HEADChatParticipantType toType, HEADChatActiveDto dto) {
        emitToParticipant(toUuid, toType, HEADChatWsEvents.CHAT_ACTIVE, dto);
    }

    @Override
    public void chatInactive(String toUuid, HEADChatParticipantType toType, HEADChatActiveDto dto) {
        emitToParticipant(toUuid, toType, HEADChatWsEvents.CHAT_INACTIVE, dto);
    }

}


