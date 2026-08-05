package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.maps.HEADAppointmentMapper;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.models.response.HEADAppointmentsResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADPageMapper;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HEADAppointmentService {

    private final HEADJobRepository headJobRepository;
    private final HEADJwtGenerator headJwtGenerator;

    @Transactional(readOnly = true)
    public HEADPageResponse<HEADAppointmentsResponse.AppointmentItem> getAppointments(
            String tab, int page, int size
    ) {
        String uuid = headJwtGenerator.getUserNamePersonalUser();
        Pageable pageable = PageRequest.of(page, size);
        Instant now = Instant.now();

        Set<HEADJobState> terminal = Set.of(
                HEADJobState.COMPLETED,
                HEADJobState.CANCELLED,
                HEADJobState.REJECTED,
                HEADJobState.EXPIRED,
                HEADJobState.WITHDRAWN,
                HEADJobState.UNASSIGNABLE
        );

        Page<HEADJob> jobsPage = switch ((tab == null ? "UPCOMING" : tab).toUpperCase()) {
            case "PAST" -> headJobRepository.findPastByClientUuid(uuid, now, terminal, pageable);
            case "ALL"  -> headJobRepository.findByClient_UuIdUserOrderByUpdatedAtDesc(uuid, pageable);
            default     -> headJobRepository.findUpcomingByClientUuid(uuid, now, terminal, pageable);
        };

        return HEADPageMapper.map(jobsPage, HEADAppointmentMapper::toItem);
    }
}
