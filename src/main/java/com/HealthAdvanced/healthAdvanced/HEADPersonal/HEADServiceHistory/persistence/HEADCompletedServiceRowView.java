package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.persistence;

import java.math.BigDecimal;
import java.time.Instant;

public interface HEADCompletedServiceRowView {
    Long getId();
    String getPatientName();
    String getServiceName();
    String getAddress();
    Instant getCompletedAt();
    Integer getDurationMinutes();
    BigDecimal getAmount();
    String getJobState();
    String getServiceMode();
}