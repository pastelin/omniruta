package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADClientStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADClientStateStore {

    private static final String PFX_STATE  = "client:state:";  // client:state:{uuid}
    private static final String SET_ACTIVE = "client:active";  // opcional
    private static final Duration TTL      = Duration.ofSeconds(120);

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private BiConsumer<String, HEADClientStateDto> onChange = (u,s) -> {};

    private final ConcurrentHashMap<String, HEADState> map = new ConcurrentHashMap<>();


    private HEADClientStateDto def() {
        return new HEADClientStateDto(false, null, null, null, 0L);
    }

    private String key(String uuid) { return PFX_STATE + uuid; }

    // -------------------------
    // helpers
    // -------------------------

    private void save(String uuid, HEADClientStateDto s) {
        try {
            String raw = om.writeValueAsString(s);
            redis.opsForValue().set(key(uuid), raw, TTL);

            // índice opcional para clientes con app abierta
            if (Boolean.TRUE.equals(s.isAppActive())) {
                redis.opsForSet().add(SET_ACTIVE, uuid);
            } else {
                redis.opsForSet().remove(SET_ACTIVE, uuid);
            }

        } catch (Exception e) {
            log.error("save client state error uuid={}", uuid, e);
        }
    }

    private HEADClientStateDto read(String uuid) {
        try {
            String raw = redis.opsForValue().get(key(uuid));
            if (raw == null) return null;

            // renueva TTL al leer (opcional)
            redis.expire(key(uuid), TTL);

            return om.readValue(raw, HEADClientStateDto.class);
        } catch (Exception e) {
            log.error("read client state error uuid={}", uuid, e);
            return null;
        }
    }

    private HEADClientStateDto compute(
            String uuid,
            java.util.function.Function<HEADClientStateDto, HEADClientStateDto> f
    ) {
        var prev = Optional.ofNullable(read(uuid)).orElse(def());
        var next = f.apply(prev);
        save(uuid, next);
        onChange.accept(uuid, next);
        return next;
    }

    public void upsert(String userUuid, boolean appActive, Long currentJobId) {
        Optional.ofNullable(userUuid)
                .ifPresent(u -> map.compute(u, (k, prev) ->
                        new HEADState(appActive, currentJobId, Instant.now())
                ));
    }

    // -------------------------
    // Public API (igual que antes)
    // -------------------------

    public HEADClientStateDto get(String uuid) {
        return read(uuid);
    }

    public void setOnChange(BiConsumer<String, HEADClientStateDto> hook) {
        this.onChange = hook != null ? hook : (u,s) -> {};
    }

    public final Predicate<HEADStaffStateDto> ELIGIBLE =
            s -> s.online() && !s.busy() && !s.hasServiceRequest();

    /** Actualiza lat/lng */
    public HEADClientStateDto updateLocation(String uuid, double lat, double lng) {
        return compute(uuid, prev ->
                new HEADClientStateDto(
                        prev.isAppActive(),
                        lat, lng,
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    /** Toggle app activa */
    public HEADClientStateDto setAppActive(String uuid, boolean active) {
        return compute(uuid, prev ->
                new HEADClientStateDto(
                        active,
                        prev.lat(), prev.lng(),
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    /** Asigna job */
    public HEADClientStateDto assignJob(String uuid, Long jobId) {
        return compute(uuid, prev ->
                new HEADClientStateDto(
                        prev.isAppActive(),
                        prev.lat(), prev.lng(),
                        jobId,
                        System.currentTimeMillis()
                ));
    }

    /** Limpia job */
    public HEADClientStateDto clearJob(String uuid) {
        return compute(uuid, prev ->
                new HEADClientStateDto(
                        prev.isAppActive(),
                        prev.lat(), prev.lng(),
                        null,
                        System.currentTimeMillis()
                ));
    }

    /** Heartbeat (corrigiendo updatedAt) */
    public HEADClientStateDto heartbeat(String uuid) {
        return compute(uuid, prev ->
                new HEADClientStateDto(
                        prev.isAppActive(),
                        prev.lat(), prev.lng(),
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    /** Clear total */
    public void clear(String uuid) {
        redis.delete(key(uuid));
        redis.opsForSet().remove(SET_ACTIVE, uuid);
        onChange.accept(uuid, null);
    }
}
