package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Component
public class HEADJobQueueStore {

    public record Session(
            long jobId,
            long createdAtMs,
            long endsAtMs,
            List<String> queue,
            Map<String, Integer> attemptsPerStaff,
            Map<String, Long> cooldownUntilMs,
            Set<String> excludedStaff,
            long lastRefillAtMs
    ) {}

    private final ConcurrentHashMap<Long, Session> sessions = new ConcurrentHashMap<>();

    public void initSession(long jobId, List<String> initialQueue, long windowMs) {
        long now = System.currentTimeMillis();

        // sin nulls, sin duplicados, preserva orden
        var qSet = Optional.ofNullable(initialQueue)
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        var q = new ArrayList<>(qSet);

        sessions.put(jobId, new Session(
                jobId,
                now,
                now + windowMs,
                q,
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                ConcurrentHashMap.newKeySet(),
                now
        ));

        log.info("[SESSION] init jobId={} queueSize={} endsAt={}", jobId, q.size(), now + windowMs);
    }

    public Session get(long jobId) { return sessions.get(jobId); }

    public boolean isExpired(long jobId) {
        var s = sessions.get(jobId);
        return s != null && System.currentTimeMillis() >= s.endsAtMs();
    }

    public void clear(long jobId) {
        log.info("[SESSION] clear jobId={}", jobId);
        sessions.remove(jobId);
    }

    public void cooldown(long jobId, String staffUuid, long ms) {
        sessions.computeIfPresent(jobId, (id, s) -> {
            s.cooldownUntilMs().put(staffUuid, System.currentTimeMillis() + ms);
            return s;
        });
    }

    public int attempts(long jobId, String staffUuid) {
        var s = sessions.get(jobId);
        if (s == null) return 0;
        return s.attemptsPerStaff().getOrDefault(staffUuid, 0);
    }

    public void mergeCandidates(long jobId, List<String> candidates, int maxAttempts) {
        sessions.computeIfPresent(jobId, (id, s) -> {
            long now = System.currentTimeMillis();

            var q = new ArrayList<>(s.queue());
            var inQueue = new HashSet<>(q); // O(1) membership
            var excluded = Optional.ofNullable(s.excludedStaff()).orElseGet(java.util.Set::of);

            Optional.ofNullable(candidates).orElseGet(java.util.List::of)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(uuid -> !excluded.contains(uuid)) // no reinsertar si cancel/reject
                    .filter(uuid -> s.attemptsPerStaff().getOrDefault(uuid, 0) < maxAttempts)
                    .filter(uuid -> {
                        Long until = s.cooldownUntilMs().get(uuid);
                        return until == null || until <= now;
                    })
                    .filter(inQueue::add) // solo los que NO estaban ya (side-effect controlado)
                    .forEach(q::add);

            return new Session(
                    s.jobId(), s.createdAtMs(), s.endsAtMs(),
                    q, s.attemptsPerStaff(), s.cooldownUntilMs(),
                    s.excludedStaff(),
                    now
            );
        });
    }

    public String pollNext(long jobId, int maxAttempts) {
        var out = new java.util.concurrent.atomic.AtomicReference<String>(null);

        sessions.computeIfPresent(jobId, (id, s) -> {
            long now = System.currentTimeMillis();
            var q = new ArrayList<>(s.queue());
            var excluded = Optional.ofNullable(s.excludedStaff()).orElseGet(Set::of);

            String next = null;
            while (!q.isEmpty()) {
                String uuid = q.remove(0);
                if (uuid == null) continue;
                if (excluded.contains(uuid)) continue;

                int a = s.attemptsPerStaff().getOrDefault(uuid, 0);
                if (a >= maxAttempts) continue;

                Long until = s.cooldownUntilMs().get(uuid);
                if (until != null && until > now) continue;

                next = uuid;
                break;
            }

            if (next != null) {
                s.attemptsPerStaff().put(next, s.attemptsPerStaff().getOrDefault(next, 0) + 1);
                out.set(next);
            }

            return new Session(
                    s.jobId(), s.createdAtMs(), s.endsAtMs(),
                    q, s.attemptsPerStaff(), s.cooldownUntilMs(),
                    s.excludedStaff(),
                    s.lastRefillAtMs()
            );
        });

        return out.get();
    }

    public boolean isQueueEmpty(long jobId) {
        var s = sessions.get(jobId);
        return s == null || s.queue() == null || s.queue().isEmpty();
    }

    // -------------------------
    // Rule #2: Cancel/Rechazo => exclude por job (NO volver a ofertar en esa sesión)
    // -------------------------
    public void exclude(long jobId, String staffUuid) {
        if (staffUuid == null) return;

        sessions.computeIfPresent(jobId, (id, s) -> {
            s.excludedStaff().add(staffUuid);

            // también lo quitamos de la cola si estuviera
            var q = new ArrayList<>(s.queue());
            q.removeIf(staffUuid::equals);

            return new Session(
                    s.jobId(), s.createdAtMs(), s.endsAtMs(),
                    q, s.attemptsPerStaff(), s.cooldownUntilMs(),
                    s.excludedStaff(),
                    s.lastRefillAtMs()
            );
        });
    }

    public boolean isExcluded(long jobId, String staffUuid) {
        var s = sessions.get(jobId);
        return s != null && s.excludedStaff() != null && s.excludedStaff().contains(staffUuid);
    }
}

