package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADCertificationResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADUpsertCertificationRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffCertification;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStaffCertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HEADUpdateCertificationService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADStaffCertificationRepository certificationRepository;

    @Transactional
    public HEADCertificationResponse execute(Long certificationId, HEADUpsertCertificationRequest request) {
        String uuIdUser = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(uuIdUser)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADStaffCertification certification = certificationRepository
                .findByIdAndStaffUser_IdUser(certificationId, staff.getIdUser())
                .orElseThrow(() -> new HEADBadRequestException("Certificación no encontrada"));

        Integer nextSortOrder = certificationRepository
                .findMaxSortOrderByStaffUserId(staff.getIdUser())
                .map(v -> v + 1)
                .orElse(0);

        certification.setTitle(Optional.ofNullable(request.title()).orElse(certification.getTitle()).trim());
        certification.setInstitution(Optional.ofNullable(request.institution()).orElse(certification.getInstitution()).trim());
        certification.setYear(Optional.ofNullable(request.year()).orElse(certification.getYear()).trim());
        certification.setSortOrder(nextSortOrder);

        certification = certificationRepository.save(certification);

        return new HEADCertificationResponse(
                certification.getId(),
                certification.getTitle(),
                certification.getInstitution(),
                certification.getYear()
        );
    }
}