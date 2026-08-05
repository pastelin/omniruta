package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.response;

public record HEADErrorAckEvent(
        String messageError,
        Boolean success
) {
}
