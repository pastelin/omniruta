package com.HealthAdvanced.healthAdvanced.HEADSchedule.storeRedis;

import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.dto.HEADScheduleProposalCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADScheduleProposalStore {

    private static final String PFX = "job:schedule:proposal:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private String key(Long jobId) { return PFX + jobId; }

    public void save(HEADScheduleProposalCache p) {
        try {
            String raw = om.writeValueAsString(p);
            redis.opsForValue().set(key(p.jobId()), raw, TTL);
        } catch (Exception e) {
            log.error("[SCHED_PROPOSAL_SAVE] jobId={} err={}", p.jobId(), e.getMessage(), e);
        }
    }

    public HEADScheduleProposalCache get(Long jobId) {
        try {
            String raw = redis.opsForValue().get(key(jobId));
            if (raw == null) return null;
            return om.readValue(raw, HEADScheduleProposalCache.class);
        } catch (Exception e) {
            log.error("[SCHED_PROPOSAL_GET] jobId={} err={}", jobId, e.getMessage(), e);
            return null;
        }
    }

    public void delete(Long jobId) {
        redis.delete(key(jobId));
    }
}
