package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADClientStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventPublisher.HEADChatPresencePublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat.HEADChatActiveConversationStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HEADPresenceConfig {

    private final HEADClientStateStore clientStateStore;
    private final HEADStaffStateStore staffStateStore;
    private final HEADChatPresencePublisher presencePublisher;
    private final HEADChatActiveConversationStore headChatActiveConversationStore;

    @PostConstruct
    void wireHooks() {
        clientStateStore.setOnChange((uuid, state) -> {
            if (state != null && state.currentJobId() != null) {
                headChatActiveConversationStore.renew(uuid);
                presencePublisher.notifyClientChange(uuid);
            }
        });

        staffStateStore.setOnChange((uuid, state) -> {
            if (state != null && state.currentJobId() != null) {
                headChatActiveConversationStore.renew(uuid);
                presencePublisher.notifyStaffChange(uuid);
            }
        });
    }
}
