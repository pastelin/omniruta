package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.request.HEADPushRequest;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.fcm.HEADFcmClient;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADPushDataKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HEADPushService {

    private final HEADPushTokenService tokenService;
    private final HEADFcmClient fcmClient;

    public void sendPush(HEADPushRequest req) {
        List<String> tokens = tokenService.findTokensForUser(req.userUuid());
        if (tokens.isEmpty()) {
            return;
        }

        Map<String, String> finalData = new HashMap<>();
        if (req.data() != null) finalData.putAll(req.data());
        finalData.put(HEADPushDataKeys.KEY_TYPE, req.type().name());

    }

    // Helpers específicos de negocio (opcional, pero cómodos)

    public void sendJobStateUpdate(String userUuid,
                                   Long jobId,
                                   String jobState,
                                   String title,
                                   String body) {

        Map<String, String> data = Map.of(
                HEADPushDataKeys.KEY_JOB_ID, String.valueOf(jobId),
                HEADPushDataKeys.KEY_JOB_STATE, jobState
        );

        sendPush(new HEADPushRequest(
                userUuid,
                HEADNotificationType.JOB_STATE_UPDATE,
                title,
                body,
                data
        ));
    }

    public void sendChatMessage(String userUuid,
                                String conversationId,
                                String senderName,
                                String snippet) {

        Map<String, String> data = Map.of(
                HEADPushDataKeys.KEY_CONVERSATION_ID, conversationId,
                HEADPushDataKeys.KEY_SENDER_NAME, senderName,
                HEADPushDataKeys.KEY_MESSAGE_SNIPPET, snippet
        );

        sendPush(new HEADPushRequest(
                userUuid,
                HEADNotificationType.CHAT_MESSAGE,
                senderName + " te ha enviado un mensaje",
                snippet,
                data
        ));
    }
}
