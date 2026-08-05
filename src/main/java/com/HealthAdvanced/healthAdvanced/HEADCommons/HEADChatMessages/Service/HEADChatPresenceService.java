package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADClientStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADClientStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.implementations.HEADPresenceRedisStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.HEADChatPresenceDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat.HEADChatActiveConversationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HEADChatPresenceService {

    private final HEADPresenceRedisStore presenceStore;
    private final HEADClientStateStore clientStateStore;
    private final HEADStaffStateStore staffStateStore;
    private final HEADChatActiveConversationStore activeConversationStore; // <- add

    public HEADChatPresenceDto getPresence(String userUuid, HEADChatParticipantType type) {

        boolean presenceOnline = presenceStore.isOnline(userUuid);
        String activeConvId = activeConversationStore.getActiveConversation(userUuid);

        return switch (type) {
            case CLIENT -> {
                var s = Optional.ofNullable(clientStateStore.get(userUuid))
                        .orElse(new HEADClientStateDto(false, null, null, null, 0L));
                yield new HEADChatPresenceDto(
                        userUuid,
                        HEADChatParticipantType.CLIENT,
                        presenceOnline,
                        Boolean.TRUE.equals(s.isAppActive()),
                        s.currentJobId(),
                        s.updatedAt(),
                        activeConvId
                );
            }
            case STAFF -> {
                var s = Optional.ofNullable(staffStateStore.get(userUuid))
                        .orElse(new HEADStaffStateDto(false, false, 0, false, null, null, false,null, 0L));
                yield new HEADChatPresenceDto(
                        userUuid,
                        HEADChatParticipantType.STAFF,
                        presenceOnline && s.online(),
                        s.online(),
                        s.currentJobId(),
                        s.updatedAt(),
                        activeConvId
                );
            }
            case ADMIN, SYSTEM -> null;
        };
    }
}
