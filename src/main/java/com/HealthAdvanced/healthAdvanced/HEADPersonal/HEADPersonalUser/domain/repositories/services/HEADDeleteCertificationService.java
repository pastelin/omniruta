package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStaffCertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADDeleteCertificationService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADStaffCertificationRepository certificationRepository;

    @Transactional
    public void execute(Long certificationId) {
        String uuIdUser = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(uuIdUser)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        var certification = certificationRepository.findByIdAndStaffUser_IdUser(certificationId, staff.getIdUser())
                .orElseThrow(() -> new HEADBadRequestException("Certificación no encontrada"));

        certification.setActive(false);
        certificationRepository.save(certification);
    }
}
