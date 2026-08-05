package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums;

public enum HEADJobState {
    OFFERED, WITHDRAWN,     // el sistema retiró la oferta (re-asignación)
    REJECTED, EXPIRED,
    SCHEDULED,
    READY,
    SCHEDULE_PENDING,
    ACCEPTED_AWAITING_START,
    ACCEPTED, EN_ROUTE, ARRIVED, STARTED, PAUSED, COMPLETED, CANCELLED,PENDING_ASSIGNMENT,UNASSIGNABLE
}

