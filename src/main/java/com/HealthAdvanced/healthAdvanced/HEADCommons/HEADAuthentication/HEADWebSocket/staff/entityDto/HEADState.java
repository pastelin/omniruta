package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto;

import java.time.Instant;

public record HEADState(boolean appActive, Long currentJobId, Instant updatedAt) {}