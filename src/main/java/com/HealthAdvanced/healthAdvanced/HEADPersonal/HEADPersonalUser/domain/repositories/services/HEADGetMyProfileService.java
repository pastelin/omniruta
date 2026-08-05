package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response.HEADMedicalInfoResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADCertificationResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADMyProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADProfileDataResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADProfileStatsResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffCertification;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffProfessionalProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetMyProfileService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADOccupationsProfilesRepository occupationProfileRepository;
    private final HEADJobRepository jobRepository;
    private final HEADFileAssetRepository headFileAssetRepository;
    private final HEADStaffProfessionalProfileRepository professionalProfileRepository;
    private final HEADStaffCertificationRepository certificationRepository;
    private final HEADOccupationPersonalUserRepository occProfileRepo;
    private final IHEADSexUserRepository sexUserRepository;

    public HEADMyProfileResponse execute() {
        String uuIdUser = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = personalUserRepository.findByUidUser(uuIdUser)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADStaffProfessionalProfile professionalProfile = professionalProfileRepository
                .findByStaffUser_IdUser(staff.getIdUser())
                .orElse(null);

        String avatarUrl = headFileAssetRepository
                .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                        HEADOwnerType.STAFF,
                        staff.getIdUser(),
                        HEADCategory.AVATAR
                )
                .map(a -> a.getUrl())
                .orElse("");

        var occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(staff.getIdUser());

        var nameStaff = HEADNameFormatters.buildStaffNameLastName(staff, occCode);


        String specialty = personalUserRepository.findPrimaryOccupationProfileId(staff.getIdUser())
                .flatMap(occupationProfileRepository::findById)
                .map(this::buildSpecialty)
                .orElse("");

        Long patients = Optional.ofNullable(
                jobRepository.countDistinctPatientsByStaff(staff.getIdUser(), HEADJobState.COMPLETED)
        ).orElse(0L);

        Long completedServices = Optional.ofNullable(
                jobRepository.countCompletedServicesByStaff(staff.getIdUser(), HEADJobState.COMPLETED)
        ).orElse(0L);
        Double avgResponseMinutes = jobRepository.avgResponseMinutesByStaff(staff.getIdUser());

        String location = Optional.ofNullable(professionalProfile)
                .map(HEADStaffProfessionalProfile::getLocationLabel)
                .orElse("");

        String experience = Optional.ofNullable(professionalProfile)
                .map(HEADStaffProfessionalProfile::getExperienceYears)
                .filter(years -> years > 0)
                .map(Object::toString)
                .orElse("");

        String responseTime = avgResponseMinutes != null
                ? "~" + Math.max(1, avgResponseMinutes.intValue()) + " min"
                : "";

        var genderList = sexUserRepository.findAll();
        var genderMap = mapGenderList(genderList);

        List<HEADCertificationResponse> certifications = certificationRepository
                .findByStaffUser_IdUserAndActiveTrueOrderBySortOrderAscIdAsc(staff.getIdUser())
                .stream()
                .map(this::toCertificationResponse)
                .toList();
        var bio = professionalProfile != null ? professionalProfile.getBio() : null;
        return new HEADMyProfileResponse(
                new HEADProfileDataResponse(
                        nameStaff,
                        specialty,
                        Optional.ofNullable(staff.getEmail()).orElse(""),
                        Optional.ofNullable(staff.getTelefono()).orElse(""),
                        location,
                        experience,
                        avatarUrl,
                        bio,
                        staff.getIdSexUser() != null ? staff.getIdSexUser().getIdSexUser() : null
                ),
                new HEADProfileStatsResponse(
                        formatNumber(patients),
                        formatNumber(completedServices),
                        responseTime
                ),
                certifications,
                genderMap
        );
    }

    private HEADCertificationResponse toCertificationResponse(HEADStaffCertification certification) {
        return new HEADCertificationResponse(
                certification.getId(),
                Optional.ofNullable(certification.getTitle()).orElse(""),
                Optional.ofNullable(certification.getInstitution()).orElse(""),
                Optional.ofNullable(certification.getYear()).orElse("")
        );
    }

    private String buildSpecialty(HEADOccupationProfile profile) {
        String occupation = Optional.ofNullable(profile.getIdOccupation())
                .map(HEADOccupations::getNameOccupation)
                .orElse("");

        return Optional.ofNullable(profile.getNameTypeProfile()).orElse(occupation);
    }

    private String formatNumber(Long value) {
        return String.format(Locale.US, "%,d", value != null ? value : 0L);
    }

    private List<HEADMedicalInfoResponse.GendersList> mapGenderList(List<HEADSexUser> genderList) {
        return genderList.stream().map(gender -> new HEADMedicalInfoResponse.GendersList(gender.getTypeSex(), gender.getIdSexUser())).toList();
    }
}
