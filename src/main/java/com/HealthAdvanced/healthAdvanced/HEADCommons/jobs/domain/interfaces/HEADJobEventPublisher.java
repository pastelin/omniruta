package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;

public interface HEADJobEventPublisher {
    void jobStateChanged(HEADJobStateChangedDto dto);
    void jobArrivalPinIssued(String clientUuid, Long jobId, String pin);

}
