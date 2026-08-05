package com.HealthAdvanced.healthAdvanced.HEADPrescription.redis;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.dto.HEADPrescriptionDraftDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class HEADPrescriptionDraftRedisStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private static final Duration TTL = Duration.ofHours(24);

    private String key(Long jobId) {
        return "head:prescription:draft:job:" + jobId;
    }

    public HEADPrescriptionDraftDto get(Long jobId) {
        var json = redis.opsForValue().get(key(jobId));
        if (json == null) return null;
        try {
            return om.readValue(json, HEADPrescriptionDraftDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void put(Long jobId, HEADPrescriptionDraftDto draft) {
        try {
            var json = om.writeValueAsString(draft);
            redis.opsForValue().set(key(jobId), json, TTL);
        } catch (Exception ignore) {}
    }

    public void delete(Long jobId) {
        redis.delete(key(jobId));
    }
}