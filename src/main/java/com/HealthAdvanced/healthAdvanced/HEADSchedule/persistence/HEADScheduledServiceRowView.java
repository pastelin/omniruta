package com.HealthAdvanced.healthAdvanced.HEADSchedule.persistence;

import java.time.Instant;

public interface HEADScheduledServiceRowView {
    Long getId();
    String getServiceName();
    String getPatientName();
    Instant getWhen();
    String getAddress();
    Integer getDurationMinutes();
    String getJobState();
    String getServiceMode();
    String getServiceDescription();
    Double getLat();
    Double getLng();
}
