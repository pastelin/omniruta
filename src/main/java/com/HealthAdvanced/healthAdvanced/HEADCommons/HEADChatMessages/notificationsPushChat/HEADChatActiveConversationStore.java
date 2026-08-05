package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class HEADChatActiveConversationStore {

    private static final String PFX = "chat:active:"; // chat:active:{userUuid} -> conversationId
    private static final Duration TTL = Duration.ofSeconds(120);

    private final StringRedisTemplate redis;

    private static String key(String userUuid) { return PFX + userUuid; }

    public void setActive(String userUuid, String conversationId) {
        redis.opsForValue().set(key(userUuid), conversationId, TTL);
    }

    public void clearActive(String userUuid, String conversationId) {
        var k = key(userUuid);
        // solo borra si coincide (evitas borrar si cambió rápido de chat)
        redis.delete(k);
    }

    public void renew(String userUuid) {
        redis.expire(key(userUuid), TTL);
    }

    public boolean isActiveIn(String userUuid, String conversationId) {
        var v = redis.opsForValue().get(key(userUuid));
        return conversationId != null && conversationId.equals(v);
    }

    public String getActiveConversation(String userUuid) {
        return redis.opsForValue().get(key(userUuid));
    }
}
