package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response.HEADServiceHistoryFiltersResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HEADServiceHistoryFiltersService {

    private final HEADJobRepository jobRepo;
    private final HEADJwtGenerator jwt;

    private static final Set<HEADJobState> CANCELLED_STATES = Set.of(
            HEADJobState.CANCELLED,
            HEADJobState.REJECTED,
            HEADJobState.EXPIRED,
            HEADJobState.WITHDRAWN,
            HEADJobState.UNASSIGNABLE
    );
    private static final Set<HEADJobState> COMPLETED_STATES = Set.of(HEADJobState.COMPLETED);

    public HEADServiceHistoryFiltersResponse getFilters() {
        String clientUuid = jwt.getUserNamePersonalUser();

        long allCount = jobRepo.countByClient_UuIdUser(clientUuid);
        long completedCount = jobRepo.countByClientUuidAndStates(clientUuid, COMPLETED_STATES);
        long cancelledCount = jobRepo.countByClientUuidAndStates(clientUuid, CANCELLED_STATES);

        var statuses = new ArrayList<HEADServiceHistoryFiltersResponse.StatusChip>();
        statuses.add(new HEADServiceHistoryFiltersResponse.StatusChip("ALL", "Todos", allCount));
        if (completedCount > 0) statuses.add(new HEADServiceHistoryFiltersResponse.StatusChip("COMPLETED", "Completado", completedCount));
        if (cancelledCount > 0) statuses.add(new HEADServiceHistoryFiltersResponse.StatusChip("CANCELLED", "Cancelado", cancelledCount));

        var serviceTypes = jobRepo.findProfileChipsForClient(clientUuid).stream()
                .map(p -> new HEADServiceHistoryFiltersResponse.ServiceType(p.getProfileId(), p.getProfileName()))
                .toList();

        return new HEADServiceHistoryFiltersResponse(serviceTypes, statuses);
    }
}