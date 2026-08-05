package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces;

import java.util.List;
import java.util.Set;

public interface HEADPresenceStore {
    void add(String sessionId, String userUuid);
    void remove(String sessionId);
    void renew(String sessionId);              // heartbeat/renovar TTL
    Set<String> sessionsOf(String userUuid);
    boolean isOnline(String userUuid);
    String userOfSession(String sessionId);
    int sessionCount(String userUuid);
    void update(String sessionId, String newUserUuid);
    List<String> sessionIdsFor(String userUuid);
}

