package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models;

import java.time.Instant;

public interface JobUuidsView {
    String getClientUuid();
    String getStaffUuid();
    Instant getStartedAt();
}
