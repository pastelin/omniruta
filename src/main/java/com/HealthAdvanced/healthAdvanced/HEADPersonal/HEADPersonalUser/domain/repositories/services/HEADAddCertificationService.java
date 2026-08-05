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
public class HEADAddCertificationService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADStaffCertificationRepository certificationRepository;

    @Transactional
    public HEADCertificationResponse execute(HEADUpsertCertificationRequest request) {
        String uuIdUser = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(uuIdUser)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new HEADBadRequestException("El título de la certificación es requerido");
        }

        Integer nextSortOrder = certificationRepository
                .findMaxSortOrderByStaffUserId(staff.getIdUser())
                .map(v -> v + 1)
                .orElse(0);


        HEADStaffCertification certification = new HEADStaffCertification();
        certification.setStaffUser(staff);
        certification.setTitle(request.title().trim());
        certification.setInstitution(Optional.ofNullable(request.institution()).orElse("").trim());
        certification.setYear(Optional.ofNullable(request.year()).orElse("").trim());
        certification.setSortOrder(nextSortOrder);
        certification.setActive(true);

        certification = certificationRepository.save(certification);

        return new HEADCertificationResponse(
                certification.getId(),
                certification.getTitle(),
                certification.getInstitution(),
                certification.getYear()
        );
    }
}