package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADCurrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HEADCurrentServiceService {

    private final HEADJobService headJobService;

    // Job ofrecido/asignado pero el staff aún no se mueve
    private static final Set<HEADJobState> WAITING_STATES = Set.of(
            HEADJobState.PENDING_ASSIGNMENT, HEADJobState.OFFERED,
            HEADJobState.ACCEPTED_AWAITING_START, HEADJobState.READY, HEADJobState.EXPIRED
    );
    // Staff en camino o ya llegó: aquí vive el tracking en vivo
    private static final Set<HEADJobState> TRACKING_STATES = Set.of(
            HEADJobState.ACCEPTED, HEADJobState.EN_ROUTE, HEADJobState.ARRIVED
    );

    /** CLIENT: devuelve el servicio activo (HEADJob) si existe, o null si no hay */
    public HEADCurrentService peekClientActive(String clientUuid) {
        var job = headJobService.currentForClient(clientUuid);
        return job == null ? null : toCurrentService(job, true);
    }

    /** STAFF: devuelve la asignación/servicio activo (HEADJob) si existe, o null si no hay */
    public HEADCurrentService peekStaffActive(String staffUuid) {
        var job = headJobService.currentForStaff(staffUuid);
        return job == null ? null : toCurrentService(job, false);
    }

    private HEADCurrentService toCurrentService(HEADJob job, boolean isClient) {
        return new HEADCurrentService(
                String.valueOf(job.getId()),
                job.getState().name(),
                screenFlowFor(job.getState(), isClient),
                Map.of("jobId", job.getId())
        );
    }

    private String screenFlowFor(HEADJobState state, boolean isClient) {
        if (WAITING_STATES.contains(state)) return isClient ? "CLIENT.SERVICE.CONFIRMATION" : "STAFF.SERVICE.OFFER";
        if (TRACKING_STATES.contains(state)) return isClient ? "CLIENT.SERVICE.TRACKING" : "STAFF.SERVICE.NAVIGATION";
        return isClient ? "CLIENT.SERVICE.IN_PROGRESS" : "STAFF.SERVICE.TASKS"; // STARTED, PAUSED
    }
}
