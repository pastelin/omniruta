package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADUpdateMyProfileRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffProfessionalProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStaffProfessionalProfileRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HEADUpdateMyProfileService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADStaffProfessionalProfileRepository professionalProfileRepository;
    private final IHEADSexUserRepository sexUserRepository;

    @Transactional
    public void execute(HEADUpdateMyProfileRequest request) {
        String uuIdUser = jwt.getUserNamePersonalUser();

        var staff = personalUserRepository.findByUidUser(uuIdUser)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        staff.setTelefono(request.numberPhone());

        if (request.sexUserId() != null) {
            HEADSexUser sexUser = sexUserRepository.findById(request.sexUserId())
                    .orElseThrow(() -> new HEADBadRequestException("Favor de ingresar el genero: " + request.sexUserId()));
            staff.setIdSexUser(sexUser);
        }

        HEADStaffProfessionalProfile profile = professionalProfileRepository
                .findByStaffUser_IdUser(staff.getIdUser())
                .orElseGet(() -> {
                    HEADStaffProfessionalProfile created = new HEADStaffProfessionalProfile();
                    created.setStaffUser(staff);
                    created.setIsPublic(true);
                    return created;
                });

        profile.setLocationLabel(Optional.ofNullable(request.location()).orElse("").trim());
        profile.setExperienceYears(request.experienceYears());
        profile.setBio(Optional.ofNullable(request.bio()).orElse("").trim());

        professionalProfileRepository.save(profile);
    }
}
