package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HEADCallRoutingRedisStore implements HEADCallRoutingStore {

    private static final String PFX_CALL       = "call:";         // call:{callId} (HASH)
    private static final String PFX_ACTIVE     = "call:active:";  // call:active:{uuid} -> callId (VALUE)

    // Ajusta a lo que te convenga:
    // - ringing corto (60-120s)
    // - active más largo (10-30 min)
    private static final Duration TTL = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    private static String callKey(String callId) { return PFX_CALL + callId; }
    private static String activeKey(String uuid) { return PFX_ACTIVE + uuid; }

    @Override
    public void bind(String callId, Participant a, Participant b) {
        var key = callKey(callId);

        // HASH: a/b
        redis.opsForHash().put(key, "aUuid", a.uuid());
        redis.opsForHash().put(key, "aType", a.type().name());
        redis.opsForHash().put(key, "bUuid", b.uuid());
        redis.opsForHash().put(key, "bType", b.type().name());

        // TTL al call
        redis.expire(key, TTL);

        // índice opcional: call activo por usuario
        redis.opsForValue().set(activeKey(a.uuid()), callId, TTL);
        redis.opsForValue().set(activeKey(b.uuid()), callId, TTL);
    }

    @Override
    public boolean isParticipant(String callId, String uuid) {
        var key = callKey(callId);
        var aUuid = (String) redis.opsForHash().get(key, "aUuid");
        var bUuid = (String) redis.opsForHash().get(key, "bUuid");
        return Objects.equals(uuid, aUuid) || Objects.equals(uuid, bUuid);
    }

    @Override
    public Optional<Participant> other(String callId, String uuid) {
        var key = callKey(callId);

        var aUuid = (String) redis.opsForHash().get(key, "aUuid");
        var bUuid = (String) redis.opsForHash().get(key, "bUuid");

        if (aUuid == null || bUuid == null) return Optional.empty();

        if (Objects.equals(uuid, aUuid)) {
            return Optional.of(readParticipant(key, "bUuid", "bType"));
        }
        if (Objects.equals(uuid, bUuid)) {
            return Optional.of(readParticipant(key, "aUuid", "aType"));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Participant> getA(String callId) {
        var key = callKey(callId);
        return Optional.ofNullable(readParticipantOrNull(key, "aUuid", "aType"));
    }

    @Override
    public Optional<Participant> getB(String callId) {
        var key = callKey(callId);
        return Optional.ofNullable(readParticipantOrNull(key, "bUuid", "bType"));
    }

    @Override
    public void remove(String callId) {
        var key = callKey(callId);

        var aUuid = (String) redis.opsForHash().get(key, "aUuid");
        var bUuid = (String) redis.opsForHash().get(key, "bUuid");

        redis.delete(key);

        if (aUuid != null) redis.delete(activeKey(aUuid));
        if (bUuid != null) redis.delete(activeKey(bUuid));
    }

    @Override
    public void touch(String callId) {
        var key = callKey(callId);
        // renueva TTL del call y los índices por usuario si existen
        redis.expire(key, TTL);

        var aUuid = (String) redis.opsForHash().get(key, "aUuid");
        var bUuid = (String) redis.opsForHash().get(key, "bUuid");

        if (aUuid != null) redis.expire(activeKey(aUuid), TTL);
        if (bUuid != null) redis.expire(activeKey(bUuid), TTL);
    }

    @Override
    public Optional<String> activeCallOf(String userUuid) {
        return Optional.ofNullable(redis.opsForValue().get(activeKey(userUuid)));
    }

    // ---- helpers ----

    private Participant readParticipant(String callKey, String uuidField, String typeField) {
        var p = readParticipantOrNull(callKey, uuidField, typeField);
        if (p == null) throw new HEADBadRequestException("CALL_ROUTING_MISSING_PARTICIPANT");
        return p;
    }

    private Participant readParticipantOrNull(String callKey, String uuidField, String typeField) {
        var uuid = (String) redis.opsForHash().get(callKey, uuidField);
        var type = (String) redis.opsForHash().get(callKey, typeField);
        if (uuid == null || type == null) return null;
        return new Participant(uuid, HEADChatParticipantType.valueOf(type));
    }
}