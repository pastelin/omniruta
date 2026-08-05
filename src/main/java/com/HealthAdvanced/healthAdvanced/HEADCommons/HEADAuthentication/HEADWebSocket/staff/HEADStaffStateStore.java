package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff;

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
import java.util.*;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADStaffStateStore {

    private static final String PFX_STATE   = "staff:state:"; // staff:state:{uuid}
    private static final String SET_ONLINE  = "staff:online";
    private static final String SET_BUSY    = "staff:busy";
    private static final Duration TTL       = Duration.ofSeconds(120);

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private final ConcurrentHashMap<String, HEADState> map = new ConcurrentHashMap<>();
    private BiConsumer<String, HEADStaffStateDto> onChange = (u,s) -> {};

    private HEADStaffStateDto def() {
        return new HEADStaffStateDto(false, false, 0,false, null, null, false, null, 0L);
    }

    private String key(String uuid) { return PFX_STATE + uuid; }

    @PostConstruct
    void init() {
        log.info("HEADStaffStateStore (Redis) init {}", System.identityHashCode(this));
    }

    // -------------------------
    // Low-level helpers
    // -------------------------

    private void save(String uuid, HEADStaffStateDto s) {
        try {
            String raw = om.writeValueAsString(s);
            redis.opsForValue().set(key(uuid), raw, TTL);

            // Índices rápidos
            if (s.online()) redis.opsForSet().add(SET_ONLINE, uuid);
            else redis.opsForSet().remove(SET_ONLINE, uuid);

            if (s.busy()) redis.opsForSet().add(SET_BUSY, uuid);
            else redis.opsForSet().remove(SET_BUSY, uuid);

        } catch (Exception e) {
            log.error("save state error uuid={}", uuid, e);
        }
    }

    private HEADStaffStateDto read(String uuid) {
        try {
            String raw = redis.opsForValue().get(key(uuid));
            if (raw == null) return null;

            // renueva TTL al leer (opcional)
            redis.expire(key(uuid), TTL);

            return om.readValue(raw, HEADStaffStateDto.class);
        } catch (Exception e) {
            log.error("read state error uuid={}", uuid, e);
            return null;
        }
    }

    private HEADStaffStateDto compute(String uuid,
                                      java.util.function.Function<HEADStaffStateDto, HEADStaffStateDto> f) {
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

    public HEADStaffStateDto get(String uuid) { return read(uuid); }


    public Map<String, HEADStaffStateDto> staffAvailable() {
        var uuids = redis.opsForSet().members(SET_ONLINE);
        if (uuids == null || uuids.isEmpty()) {
            return Map.of();
        }

        return uuids.stream()
                .map(uuid -> {
                    var state = read(uuid);
                    if (state == null) {
                        redis.opsForSet().remove(SET_ONLINE, uuid);
                        redis.opsForSet().remove(SET_BUSY, uuid);
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(uuid, state);
                })
                .filter(Objects::nonNull)
                .filter(entry -> {
                    var s = entry.getValue();
                    return s.online() && !s.busy() && !s.hasServiceRequest();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, HEADStaffStateDto> staffOnline() {
        var uuids = redis.opsForSet().members(SET_ONLINE);
        if (uuids == null || uuids.isEmpty()) return Map.of();

        return uuids.stream()
                .map(uuid -> Map.entry(uuid, read(uuid)))
                .filter(e -> {
                    var s = e.getValue();
                    return s != null && s.online();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public void setOnChange(BiConsumer<String, HEADStaffStateDto> hook) {
        this.onChange = hook != null ? hook : (u,s) -> {};
    }

    public HEADStaffStateDto setOnline(String uuid, boolean online) {
        log.info("setOnline uuid={} online={}", uuid, online);
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        online, prev.busy(), prev.countRejected(), prev.hasServiceRequest(),
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto setBusy(String uuid, boolean busy, Long jobIdCurrent) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), busy, prev.countRejected(), prev.hasServiceRequest(),
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        jobIdCurrent,
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto assignJob(String uuid, Long jobId) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), true, prev.countRejected(), false,
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        jobId,
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto hasServiceRequest(String uuid, Long jobId) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), false, prev.countRejected(), true,
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        jobId,
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto rejectedService(String uuid, Long jobId) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), false, prev.countRejected() + 1, false,
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        jobId,
                        System.currentTimeMillis()
                ));
    }

    /** Toggle app activa */
    public HEADStaffStateDto setAppActive(String uuid, boolean active) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(),
                        prev.busy(),
                        prev.countRejected(),
                        prev.hasServiceRequest(),
                        prev.lat(),
                        prev.lng(),
                        active,
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto clearJob(String uuid) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), false, prev.countRejected(), prev.hasServiceRequest(),
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        null,
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto releaseOffer(String uuid) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), prev.busy(), prev.countRejected(), false,
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        null,
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto updateLocation(String uuid, double lat, double lng) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), prev.busy(), prev.countRejected(), prev.hasServiceRequest(),
                        lat, lng,
                        prev.isAppActive(),
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    public HEADStaffStateDto heartbeat(String uuid) {
        return compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(), prev.busy(), prev.countRejected(), prev.hasServiceRequest(),
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        prev.currentJobId(),
                        System.currentTimeMillis()
                ));
    }

    public boolean isEligible(String uuid) {
        var s = read(uuid);
        return s != null && s.online() && !s.busy() && !s.hasServiceRequest();
    }

    /** Con Redis ya no hace falta evict local, pero lo dejo por compatibilidad. */
    public int evictStale(long maxAgeMillis) {
        // En Redis expira solo por TTL
        return 0;
    }

    public void clear(String uuid) {
        redis.delete(key(uuid));
        redis.opsForSet().remove(SET_ONLINE, uuid);
        redis.opsForSet().remove(SET_BUSY, uuid);
        onChange.accept(uuid, null);
    }

    public HEADStaffStateDto setAvailability(String uuid, boolean online) {
        return compute(uuid, prev -> new HEADStaffStateDto(
                online,
                false,
                prev.countRejected(),
                false,
                prev.lat(), prev.lng(),
                prev.isAppActive(),
                null,
                System.currentTimeMillis()));
    }

    public void markJobFinished(String uuid) {
        compute(uuid, prev ->
                new HEADStaffStateDto(
                        prev.online(),
                        false,
                        prev.countRejected(),
                        false,
                        prev.lat(), prev.lng(),
                        prev.isAppActive(),
                        null,
                        System.currentTimeMillis()
                )
        );
    }
}




