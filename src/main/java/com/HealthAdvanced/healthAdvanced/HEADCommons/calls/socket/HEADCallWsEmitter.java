package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HEADCallWsEmitter {

    private final HEADWsEmitter emitter;

    public void toUser(String userUuid, HEADChatParticipantType type, String event, Object payload) {
        if (type == HEADChatParticipantType.STAFF) {
            // OJO: tu toUser() manda a /staff
            emitter.toUser(userUuid, event, payload);
        } else {
            // tu emitToClient() manda a /client
            emitter.emitToClient(userUuid, event, payload);
        }
    }
}