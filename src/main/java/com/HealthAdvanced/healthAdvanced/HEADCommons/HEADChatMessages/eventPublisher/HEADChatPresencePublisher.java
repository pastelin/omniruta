package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventPublisher;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service.HEADChatPresenceService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADChatPresencePublisher {

    private final HEADChatEventPublisher chatEvents;
    private final HEADChatPresenceService presenceService;

    public void notifyClientChange(String clientUuid) {
        var dto = presenceService.getPresence(clientUuid, HEADChatParticipantType.CLIENT);
        chatEvents.presenceUpdated(dto); // emit al socket
    }

    public void notifyStaffChange(String staffUuid) {
        var dto = presenceService.getPresence(staffUuid, HEADChatParticipantType.STAFF);
        chatEvents.presenceUpdated(dto);
    }
}

