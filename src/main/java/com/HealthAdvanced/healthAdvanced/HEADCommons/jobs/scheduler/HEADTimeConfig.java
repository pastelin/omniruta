package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class HEADTimeConfig {
    @Bean
    public Clock clock(@Value("${app.timezone:UTC}") String tz) {
        return Clock.system(ZoneId.of(tz));
    }
}
