package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.scheduler;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADInvalidJobTransitionException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class HEADJobScheduler {

    private final HEADJobRepository repo;
    private final HEADJobService svc;

    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void expireOffers() {
        var now = Instant.now();
        repo.findTop100ByStateAndOfferExpiresAtBefore(HEADJobState.OFFERED, now)
                .forEach(job -> {
                    try { svc.expire(job.getId()); }
                    catch (Exception ignored) {}
                });
    }
}

