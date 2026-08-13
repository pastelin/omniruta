package com.HealthAdvanced.healthAdvanced.HEADLiveTracking.service;

import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.dto.HEADVehicleLocationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Guarda/lee la última ubicación conocida de cada vehículo en Redis.
 * Reutiliza el StringRedisTemplate ya auto-configurado por spring-boot-starter-data-redis
 * (mismo Redis que usan HEADPresenceRedisStore, HEADStaffStateStore, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HEADTrackingLocationService {

    private static final String KEY_PREFIX = "tracking:vehicle:";
    private static final String ACTIVE_SET_KEY = "tracking:vehicles:active";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public HEADVehicleLocationDto save(HEADVehicleLocationDto dto) {
        if (dto.getVehicleId() == null || dto.getVehicleId().isBlank()) {
            throw new IllegalArgumentException("vehicleId es obligatorio");
        }
        if (dto.getLat() == null || dto.getLng() == null) {
            throw new IllegalArgumentException("lat/lng son obligatorios");
        }
        if (dto.getRecordedAt() == null) {
            dto.setRecordedAt(Instant.now());
        }

        String json = writeJson(dto);
        redis.opsForValue().set(KEY_PREFIX + dto.getVehicleId(), json, TTL);
        redis.opsForSet().add(ACTIVE_SET_KEY, dto.getVehicleId());
        return dto;
    }

    public HEADVehicleLocationDto get(String vehicleId) {
        String json = redis.opsForValue().get(KEY_PREFIX + vehicleId);
        if (json == null) {
            redis.opsForSet().remove(ACTIVE_SET_KEY, vehicleId);
            return null;
        }
        return readJson(json);
    }

    /** Lista las últimas ubicaciones conocidas de todos los vehículos activos (no expirados). */
    public List<HEADVehicleLocationDto> listActive() {
        Set<String> ids = redis.opsForSet().members(ACTIVE_SET_KEY);
        List<HEADVehicleLocationDto> out = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return out;

        for (String id : ids) {
            var dto = get(id); // limpia del set si ya expiró en Redis
            if (dto != null) out.add(dto);
        }
        return out;
    }

    private String writeJson(HEADVehicleLocationDto dto) {
        try {
            return MAPPER.writeValueAsString(dto);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar la ubicación", e);
        }
    }

    private HEADVehicleLocationDto readJson(String json) {
        try {
            return MAPPER.readValue(json, HEADVehicleLocationDto.class);
        } catch (Exception e) {
            log.error("[tracking] JSON inválido en Redis, se descarta: {}", e.toString());
            return null;
        }
    }
}
