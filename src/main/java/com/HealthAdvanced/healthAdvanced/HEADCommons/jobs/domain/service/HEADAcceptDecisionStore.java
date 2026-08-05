package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;


import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADAcceptDecisionCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADAcceptDecisionStore {

    private static final String PFX = "job:accept:decision:";
    private static final Duration TTL = Duration.ofSeconds(60); // ajusta 30-60s

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private String key(Long jobId) { return PFX + jobId; }

    /** Se llama cuando el staff ACEPTA la oferta y muestras la pantalla de decisión. */
    public void save(HEADAcceptDecisionCache p) {
        try {
            String raw = om.writeValueAsString(p);
            redis.opsForValue().set(key(p.jobId()), raw, TTL);
        } catch (Exception e) {
            log.error("[ACCEPT_DECISION_SAVE] jobId={} staffId={} err={}",
                    p.jobId(), p.staffId(), e.getMessage(), e);
        }
    }

    public HEADAcceptDecisionCache get(Long jobId) {
        try {
            String raw = redis.opsForValue().get(key(jobId));
            if (raw == null) return null;
            return om.readValue(raw, HEADAcceptDecisionCache.class);
        } catch (Exception e) {
            log.error("[ACCEPT_DECISION_GET] jobId={} err={}", jobId, e.getMessage(), e);
            return null;
        }
    }

    public void delete(Long jobId) {
        redis.delete(key(jobId));
    }

    /**
     * Consume (one-shot): valida que la decisión pertenezca a este staff y la borra.
     * Retorna:
     *  - true  => ok, puedes proceder
     *  - false => expiró / no existe / no coincide staff
     */
    public boolean consume(Long jobId, Long staffId) {
        try {
            HEADAcceptDecisionCache cur = get(jobId);
            if (cur == null) {
                log.warn("[ACCEPT_DECISION_CONSUME] jobId={} staffId={} result=EXPIRED_OR_MISSING", jobId, staffId);
                return false;
            }
            if (!cur.staffId().equals(staffId)) {
                log.warn("[ACCEPT_DECISION_CONSUME] jobId={} staffId={} ownerStaffId={} result=NOT_OWNER",
                        jobId, staffId, cur.staffId());
                return false;
            }
            delete(jobId);
            return true;
        } catch (Exception e) {
            log.error("[ACCEPT_DECISION_CONSUME] jobId={} staffId={} err={}", jobId, staffId, e.getMessage(), e);
            return false;
        }
    }

    /** Útil para mostrar en UI “expira en Xs” o debug. */
    public Long ttlSeconds(Long jobId) {
        try {
            return redis.getExpire(key(jobId));
        } catch (Exception e) {
            log.error("[ACCEPT_DECISION_TTL] jobId={} err={}", jobId, e.getMessage(), e);
            return null;
        }
    }
}


