package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.jobws;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADJobArrivalPinDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.JobDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;// <-- ajusta el paquete real
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.JOB_STATE_CHANGED;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADEventsClientConst.JOB_ARRIVAL_PIN_ISSUED;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADSocketJobEventPublisher implements HEADJobEventPublisher {

    private final HEADWsEmitter emitter;

    @Override
    public void jobStateChanged(HEADJobStateChangedDto dto) {
        //emitter.emitToClientJob(domain.jobId(), JOB_STATE_CHANGED, domain);

        if (dto.staffUuid() != null && !dto.staffUuid().isBlank()) {
            emitter.toUser(dto.staffUuid(), JOB_STATE_CHANGED, dto);
        }

        if (dto.clientUuid() != null && !dto.clientUuid().isBlank()) {
            dto = HEADJobStateChangedDto.modifiedForClientOf(dto);
            emitter.emitToClient(dto.clientUuid(), JOB_STATE_CHANGED, dto);
        }
    }

    @Override
    public void jobArrivalPinIssued(String clientUuid, Long jobId, String pin) {
        if (clientUuid == null || clientUuid.isBlank()) return;

        emitter.emitToClient(clientUuid, JOB_ARRIVAL_PIN_ISSUED, new HEADJobArrivalPinDto(jobId, pin));
    }

}

