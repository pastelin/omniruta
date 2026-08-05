package com.HealthAdvanced.healthAdvanced.HEADSchedule.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduleDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetScheduleDetailService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADJobRepository jobRepository;
    private final HEADGetMyScheduleService scheduleMapperService;

    public HEADScheduleDetailResponse execute(Long jobId) {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        var row = jobRepository.findScheduledServiceDetailForStaff(jobId, staff.getIdUser())
                .orElseThrow(() -> new HEADBadRequestException("Servicio no encontrado"));

        return new HEADScheduleDetailResponse(
                scheduleMapperService.toResponse(row)
        );
    }
}