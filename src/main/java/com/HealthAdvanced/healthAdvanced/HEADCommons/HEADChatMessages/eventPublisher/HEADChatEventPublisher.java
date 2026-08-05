package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventPublisher;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.interfaces.HEADChatUnreadSummary;

import java.util.List;


public interface HEADChatEventPublisher {

    /** Mensaje nuevo creado. */
    void messageSent(HEADChatMessageDto dto);

    /** Cambio de estado de mensajes (DELIVERED / READ). */
    void messageStatusUpdated(HEADChatMessageStatusUpdateDto dto, HEADChatMessage message);

    /** Typing “escribiendo…”. */
    void typingUpdated(HEADChatTypingUpdateDto dto, String uuidUser, HEADChatParticipantType type);

    /** Resumen de no leídos para un usuario. */
    void unreadSummaryUpdated(String userUuid,
                              HEADChatParticipantType type,
                              List<HEADChatUnreadSummary> summary);

    void messagesHistory(String userUuid, HEADChatParticipantType type, HEADChatHistoryPageDto headChatHistoryPageDto);
    void presenceUpdated(HEADChatPresenceDto presence);
    void chatActive(String toUuid, HEADChatParticipantType toType, HEADChatActiveDto dto);
    void chatInactive(String toUuid, HEADChatParticipantType toType, HEADChatActiveDto dto);
}
