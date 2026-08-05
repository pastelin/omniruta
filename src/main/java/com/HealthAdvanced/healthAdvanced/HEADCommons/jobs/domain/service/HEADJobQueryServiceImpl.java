package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobQueryService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class HEADJobQueryServiceImpl implements HEADJobQueryService {

    private final HEADJobRepository jobRepo;

    private static final Set<HEADJobState> ACTIVE_STATES = EnumSet.of(
            HEADJobState.ACCEPTED_AWAITING_START,
            HEADJobState.PENDING_ASSIGNMENT, HEADJobState.OFFERED,
            HEADJobState.ACCEPTED, HEADJobState.EN_ROUTE, HEADJobState.ARRIVED,
            HEADJobState.STARTED, HEADJobState.PAUSED, HEADJobState.READY
    );

    public HEADJobQueryServiceImpl(HEADJobRepository jobRepo) {
        this.jobRepo = jobRepo;
    }

    @Override
    public List<Long> findActiveJobIdsForStaffUserId(Long staffUserId) {
        if (staffUserId == null) return List.of();
        return jobRepo.findIdsByStaffUserIdAndStates(staffUserId, ACTIVE_STATES);
    }
    @Override
    public List<Long> findActiveJobIdsForClientUserId(Long clientUserId) {
        if (clientUserId == null) return List.of();
        return jobRepo.findIdsByClientUserIdAndStates(clientUserId, ACTIVE_STATES);
    }

    @Override
    public List<HEADJob> findActiveJobCurrentsForStaffUserId(Long staffUserId) {
        if (staffUserId == null) return  List.of();
        ACTIVE_STATES.add(HEADJobState.EXPIRED);
        return jobRepo.findLatestActiveForStaff(staffUserId, ACTIVE_STATES);
    }

    @Override
    public List<HEADJob> findActiveJobCurrentsForClientUserId(Long clientUserId) {
        if(clientUserId == null) return List.of();
        ACTIVE_STATES.add(HEADJobState.EXPIRED);
        return jobRepo.findLatestActiveForClient(clientUserId, ACTIVE_STATES);
    }


}