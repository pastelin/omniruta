package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class HEADStaffStateJanitor {
    private final HEADStaffStateStore state;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void sweep() {
        int n = state.evictStale(90_000);
        if (n > 0) log.info("Evicted {} stale staff states", n);
    }
}
