package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.listener;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADAgeUtil;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobStateChangedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffJobMaterialDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackageOptionMaterialRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADJobStateChangedListener {

    private final HEADJobRepository jobRepo;
    private final HEADJobEventPublisher wsEvents;
    private final HEADFileAssetRepository fileAssetRepository;
    private final HEADStaffStateStore staffStateStore;
    private final HEADStaffCredentialRepository credentialNoRepo;
    private final HEADOccupationPersonalUserRepository occProfileRepo;
    private final HEADPackageOptionMaterialRepository materialRepository;

    @Transactional(readOnly = true)
    @EventListener
    public void onJobStateChanged(HEADJobStateChangedEvent ev) {

            // 1) Cargar job ya persistido (con staff/client si ocupas)
            HEADJob job = jobRepo.findById(ev.jobId())
                    .orElse(null);

            if (job == null) {
                log.warn("[onJobStateChanged] jobId={} not found for event", ev.jobId());
                return;
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
            var isRequiredPrescription = job.getRequest().getPkg().getRequiredPrescription();
            String prescriptionClient = null;

            if (isRequiredPrescription) {
                var getUrlPrescription = fileAssetRepository.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT, job.getClient().getIdUser(), HEADCategory.PRESCRIPTION).orElse(null);
                prescriptionClient = getUrlPrescription != null ? getUrlPrescription.getUrl() : null;
            }

            HEADServiceRequestClient request = job.getRequest();
            HEADPackageOption option = request.getPackageOption();

            List<HEADStaffJobMaterialDto> materialsDto = new ArrayList<>();
            if (option.getIncludesMaterials()) {
                var optionId = job.getRequest().getPackageOption().getId();
                var materials = materialRepository.findAllActiveByPackageOptionId(optionId);
                materialsDto = materials.stream().map(material -> new HEADStaffJobMaterialDto(material.getMaterialName(),material.getQuantityLabel(),material.getNotes())).toList();
            }
            // 2) Armar tu DTO de socket (el fuerte)
            HEADJobStateChangedDto dto = HEADJobStateChangedDto.of(
                    job,
                    ev.prev() != null ? ev.prev().name() : null,
                    ev.at(),
                    ev.actorUuid(),
                    avatarClient != null ? avatarClient.getUrl() : null,
                    avatarStaff != null ? avatarStaff.getUrl() : null,
                    getCredentialNo,
                    occCode,
                    getYearsClient,
                    prescriptionClient,
                    materialsDto
            );
            log.warn("[ChangedState] success after accept jobId={} staffUuid={} statePrev={} stateNext={}",
                    ev.jobId(), ev.actorUuid(), ev.prev(), job.getState());

            wsEvents.jobStateChanged(dto);
        } catch (Exception ex) {
            log.warn("[ChangedState] failed after accept jobId={} staffUuid={} err={}",
                    ev.jobId(), ev.actorUuid(), ex.toString(), ex);
        }
    }
}

