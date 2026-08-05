package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADAgeUtil;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADCallStateUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffJobMaterialDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackageOptionMaterialRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADCurrentStepsService {
    private final HEADJobService headJobService;
    private final HEADJobEventPublisher wsEvents;
    private final HEADFileAssetRepository fileAssetRepository;
    private final HEADStaffStateStore staffStateStore;
    private final HEADStaffCredentialRepository credentialNoRepo;
    private final HEADOccupationPersonalUserRepository occProfileRepo;
    private final HEADJwtGenerator jwtAccessToken;
    private final HEADPackageOptionMaterialRepository materialRepository;

    public HEADJobStateChangedDto currentStateClient() {
        var userCurrentClient = jwtAccessToken.getUserNamePersonalUser();
        var headJob = headJobService.currentForClient(userCurrentClient);
        return stateChangeCurrent(headJob,userCurrentClient, true);
    }

    public HEADJobStateChangedDto currentStateStaff() {
        var userCurrentClient = jwtAccessToken.getUserNamePersonalUser();
        var headJob = headJobService.currentForStaff(userCurrentClient);
        return stateChangeCurrent(headJob, userCurrentClient, false);
    }


    @Transactional(readOnly = true)
    private HEADJobStateChangedDto stateChangeCurrent(HEADJob job, String userUuid, Boolean isClient) {


        if (job == null) {
            throw new HEADBadRequestException("No tiene Servicio actual");
        }

        try {
            var staffUuid = job.getStaffUuid();
            if ((job.getState() == HEADJobState.COMPLETED || job.getState() == HEADJobState.CANCELLED) && staffUuid != null) {
                staffStateStore.markJobFinished(staffUuid);
                log.info("[StateChangedListener] freed staff uuid={} after jobId={} state={}",
                        staffUuid, job.getId(), job.getState());
            }

            HEADFileAsset avatarStaff = null;
            String getCredentialNo = null;
            HEADOccupationCode occCode = null;

            if (job.getStaffUser() != null) {
                avatarStaff = fileAssetRepository.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.STAFF, job.getStaffUser().getIdUser(), HEADCategory.AVATAR).orElse(null);
                getCredentialNo = credentialNoRepo.findApprovedGlobalLicenseNo(job.getStaffUser().getIdUser()).orElse(null);
                occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(job.getStaffUser().getIdUser());
            }

            var avatarClient = fileAssetRepository.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT, job.getClient().getIdUser(), HEADCategory.AVATAR).orElse(null);
            var getYearsClient = HEADAgeUtil.ageYears(job.getClient().getFechaNacimiento());


            HEADServiceRequestClient request = job.getRequest();
            HEADPackageOption option = request.getPackageOption();

            List<HEADStaffJobMaterialDto> materialsDto = new ArrayList<>();
            if (option.getIncludesMaterials()) {
                var optionId = job.getRequest().getPackageOption().getId();
                var materials = materialRepository.findAllActiveByPackageOptionId(optionId);
                materialsDto = materials.stream().map(material -> new HEADStaffJobMaterialDto(material.getMaterialName(),material.getQuantityLabel(),material.getNotes())).toList();
            }

            HEADFileAsset prescription = job.getRequest().getPrescriptionAsset();

            // 2) Armar tu DTO de socket (el fuerte)
            HEADJobStateChangedDto dto = HEADJobStateChangedDto.of(
                    job,
                    job.getState().name(),
                    Instant.now(),
                    userUuid,
                    avatarClient != null ? avatarClient.getUrl() : null,
                    avatarStaff != null ? avatarStaff.getUrl() : null,
                    getCredentialNo,
                    occCode,
                    getYearsClient,
                    isClient ? null : prescription.getUrl(),
                    isClient ? new ArrayList<>() : materialsDto
            );

            wsEvents.jobStateChanged(dto);
            return dto;
        } catch (Exception ex) {
            log.error("[ChangedState] failed after accept jobId={} staffUuid={} err={}",
                    job.getId(), userUuid, ex.toString(), ex);
            throw new HEADBadRequestException("Error al obtener el estado actual del servicio");
        }
    }
}
