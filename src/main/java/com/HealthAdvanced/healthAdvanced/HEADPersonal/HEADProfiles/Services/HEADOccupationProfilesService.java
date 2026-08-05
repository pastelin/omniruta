package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStepCurrentPersonalResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADServiceProfileItemDto;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.servicesMap.HEADPersonalUserMapService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationPersonalUserResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsProfilesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.ServicesMap.HEADOccupationProfileMapInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEHOFileUploadUtil;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class HEADOccupationProfilesService implements HEADOccupationsProfilesInterface {
    @Autowired
    private HEADOccupationsRepository headOccupationsRepository;
    @Autowired
    private HEADOccupationsProfilesRepository headOccupationsProfilesRepository;
    @Autowired
    private HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    @Autowired
    @Qualifier("HEADOccupationProfileMapService")
    private HEADOccupationProfileMapInterface headOccupationProfileMapInterface;
    @Autowired
    private HEADJwtGenerator headJwtGenerator;
    @Autowired
    private HEADPersonalUserRepository personaUserRepository;
    @Autowired
    private HEADPersonalUserMapService personalMapService;
    @Autowired
    private HEADStepCurrentPersonalInterface headStepCurrentPersonalInterface;
    @Autowired
    private HEADStepCatalogueRepository headStepCatalogueRepository;
    @Autowired
    private HEHOFileUploadUtil docsUtil;
    @Autowired
    private HEADFileAssetRepository repoAssets;
    @Autowired
    private HEADDocumentsRepository documentsRepo;
    @Autowired
    private HEADAppNavigatorService navigator;

    @Override
    public HEADOccupationsProfilesDto headSetOccupations() {
        var getOccupations = headOccupationsRepository.findAll();
        var getOccupationsProfiles = headOccupationsProfilesRepository.findAll();
        return headOccupationProfileMapInterface.headOccupationsProfilesMapDto(getOccupations,getOccupationsProfiles);
    }

    @Override
    public ResponseEntity<?> saveOccupationPersonalUser(List<Long> idOccupationProfile) {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        var userDetail = personaUserRepository.findByUidUser(getUUID).orElse(null);
        HEADOccupationPersonalUserResponse occupationPersonal = new HEADOccupationPersonalUserResponse();
        if (userDetail == null) {
            occupationPersonal.setIsSaveOccupationSelected(false);
            occupationPersonal.setSelectedCount(idOccupationProfile.size());
            return new ResponseEntity<>(occupationPersonal, HttpStatus.NOT_FOUND);
        }

        headOccupationPersonalUserRepository.deleteByIdPersonalUser(userDetail);

        var listProfiles = headOccupationPersonalUserRepository.findByIdPersonalUser(userDetail).orElse(new ArrayList<>());
        List<Long> requestedIds = Optional.of(idOccupationProfile)
                .orElseGet(Collections::emptyList);

        Set<Long> existingIds = Optional.of(listProfiles)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(HEHOOccupationPersonalUser::getIdOccupationProfile)
                .filter(Objects::nonNull)
                .map(HEADOccupationProfile::getIdOccupationProfile)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, HEADOccupationProfile> profilesById = headOccupationsProfilesRepository
                .findAllById(requestedIds)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        HEADOccupationProfile::getIdOccupationProfile,
                        Function.identity(),
                        (a, b) -> a
                ));

        List<HEHOOccupationPersonalUser> newProfilesUser = requestedIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !existingIds.contains(id))
                .map(profilesById::get)               // puede dar null si no existe
                .filter(Objects::nonNull)
                .map(profile -> headOccupationProfileMapInterface
                        .headOccupationPersonaluserMaps(userDetail, profile))
                .toList();



        headOccupationPersonalUserRepository.saveAll(newProfilesUser);
        headStepCurrentPersonalInterface.staffCompleteSub(userDetail.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.OCCUPATION_PROFILES.name());
        occupationPersonal.setIsSaveOccupationSelected(true);
        occupationPersonal.setSelectedCount(newProfilesUser.size());
        var appState = navigator.resolveStateForUuid(userDetail.getUidUser());
        occupationPersonal.setStepCurrent(appState.stepStatus());
        occupationPersonal.setHeadAppStateDTO(appState);
        return new ResponseEntity<>(occupationPersonal, HttpStatus.OK);
    }

    @Override
    public List<HEADServiceProfileItemDto> listProfilesUber() {
        String uuid = headJwtGenerator.getUserNamePersonalUser();
        var user = personaUserRepository.findByUidUser(uuid).orElse(null);

        // Perfiles globales
        var profiles = headOccupationsProfilesRepository.findAll();          // HEADOccupationProfile
        // Seleccionados del usuario
        var selectedIds = (user == null) ? Set.<Long>of()
                : headOccupationPersonalUserRepository.findByIdPersonalUser(user).orElseGet(List::of)
                .stream()
                .map(l -> l.getIdOccupationProfile().getIdOccupationProfile())
                .collect(java.util.stream.Collectors.toSet());

        // Mapea a DTO
        return profiles.stream().map(p -> {
            HEADServiceProfileItemDto dto = new HEADServiceProfileItemDto();
            dto.setId(p.getIdOccupationProfile());
            dto.setTitle(p.getNameTypeProfile());
            var getIcon = repoAssets.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.SYSTEM,p.getIdOccupationProfile(), HEADCategory.SERVICE_ICON).orElse(null);
            var occ = p.getIdOccupation(); // HEADOccupations
            dto.setArea(occ != null ? occ.getNameOccupation() : "Perfil");
            dto.setProfileType(p.getNameTypeProfile());   // o algún subtipo si tienes
            dto.setTags(List.of());                       // rellena si tienes metadata
            dto.setIconKey(getIcon != null ? getIcon.getUrl() : "profile");

            // Estado / documentos (opcional si tienes util)
            boolean canGo = false;
            Integer ok = null, req = null;
            if (user != null && docsUtil != null) {
                // Verifica si puede estar online
                canGo = docsUtil.canGoOnline(user.getIdUser(), p.getIdOccupationProfile());

                // Obtén el total de documentos requeridos para este perfil
                Set<Integer> requiredDocs =
                        docsUtil.getRequiredDocIdsByOccProfile(p.getIdOccupationProfile());  // 👈 nuevo helper
                req = requiredDocs.size();

                // Obtén cuántos documentos ya están aprobados para este perfil
                Set<Integer> approvedDocs =
                        documentsRepo.findApprovedDocIdsByUserAndOccProfile(
                                user.getIdUser(), p.getIdOccupationProfile());
                ok = approvedDocs.size();
            }
            dto.setEnabled(true);
            dto.setCanGoOnline(canGo);
            dto.setDocsApproved(ok);
            dto.setDocsRequired(req);

            // Líneas grises (descritas por backend; no hardcode front)
            var lines = new java.util.ArrayList<String>();
            lines.add("Edad: Más de 18");
            if (ok != null) {
                lines.add("Docs: " + ok + "/" + req + " aprobados");
            }
            dto.setLines(lines);

            dto.setPreselected(selectedIds.contains(p.getIdOccupationProfile()));
            return dto;
        }).toList();
    }

}
