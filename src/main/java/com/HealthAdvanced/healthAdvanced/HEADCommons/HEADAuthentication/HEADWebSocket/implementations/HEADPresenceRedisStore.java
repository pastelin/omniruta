package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.implementations;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HEADPresenceRedisStore implements HEADPresenceStore {

    private static final String PFX_PRESENCE = "presence:"; // Set(sessionIds)
    private static final String PFX_SESSION  = "session:";   // Value(userUuid)
    private static final Duration TTL = Duration.ofSeconds(120);

    private final StringRedisTemplate redis;

    private static String sKey(String sessionId) { return PFX_SESSION + sessionId; }
    private static String uKey(String userUuid)  { return PFX_PRESENCE + userUuid; }

    @Override
    public void add(String sessionId, String userUuid) {
        // session -> user (con TTL)
        redis.opsForValue().set(sKey(sessionId), userUuid, TTL);
        // user -> set(sessionId)
        redis.opsForSet().add(uKey(userUuid), sessionId);
        // TTL suave al set del usuario
        redis.expire(uKey(userUuid), TTL);
    }

    @Override
    public void remove(String sessionId) {
        var key = sKey(sessionId);
        var userUuid = redis.opsForValue().get(key);

        redis.delete(key);

        Optional.ofNullable(userUuid).ifPresent(u -> {
            var uk = uKey(u);
            redis.opsForSet().remove(uk, sessionId);
            var size = redis.opsForSet().size(uk);
            if (size == null || size == 0) redis.delete(uk);
        });
    }

    @Override
    public void renew(String sessionId) {
        var key = sKey(sessionId);
        var userUuid = redis.opsForValue().get(key);
        if (userUuid == null) return;

        redis.expire(key, TTL);
        // Renueva TTL del set del usuario (si lo estás usando)
        redis.expire(uKey(userUuid), TTL);
    }

    /** === REAUTH: mover una sesión a otro usuario === */
    @Override
    public void update(String sessionId, String newUserUuid) {
        var key = sKey(sessionId);
        var oldUser = redis.opsForValue().get(key);

        if (oldUser == null) { add(sessionId, newUserUuid); return; }
        if (Objects.equals(oldUser, newUserUuid)) { renew(sessionId); return; }

        var oldKey = uKey(oldUser);
        var newKey = uKey(newUserUuid);

        // Pipeline para minimizar RTT y mantener consistencia básica
        redis.executePipelined((RedisCallback<Object>) connection -> {
            redis.opsForValue().set(key, newUserUuid, TTL);
            redis.opsForSet().remove(oldKey, sessionId);
            redis.opsForSet().add(newKey, sessionId);
            redis.expire(newKey, TTL);
            return null;
        });

        // Limpieza: si el set viejo queda vacío, bórralo
        var size = redis.opsForSet().size(oldKey);
        if (size != null && size == 0) redis.delete(oldKey);
    }

    @Override
    public List<String> sessionIdsFor(String userUuid) {
        // Reusa la limpieza perezosa de sessionsOf y convierte a lista inmutable
        return sessionsOf(userUuid).stream().toList();
    }

    @Override
    public Set<String> sessionsOf(String userUuid) {
        var key = uKey(userUuid);
        var sids = redis.opsForSet().members(key);
        if (sids == null || sids.isEmpty()) return Set.of();

        // Limpieza perezosa: filtra SIDs expirados y remueve los muertos del set
        var live = sids.stream()
                .filter(Objects::nonNull)
                .filter(sid -> Boolean.TRUE.equals(redis.hasKey(sKey(sid))))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Remueve del set los que no sobrevivieron
        var dead = sids.stream()
                .filter(Objects::nonNull)
                .filter(sid -> !live.contains(sid))
                .toArray(String[]::new);
        if (dead.length > 0) redis.opsForSet().remove(key, (Object[]) dead);

        if (live.isEmpty()) redis.delete(key);
        return Collections.unmodifiableSet(live);
    }

    @Override
    public boolean isOnline(String userUuid) {
        return !sessionsOf(userUuid).isEmpty();
    }

    @Override
    public String userOfSession(String sessionId) {
        return redis.opsForValue().get(sKey(sessionId));
    }

    @Override
    public int sessionCount(String userUuid) {
        return sessionsOf(userUuid).size();
    }
}



