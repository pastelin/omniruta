package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADFileMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.map.HEADChatMap;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADScreenType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEADFileStorageService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADChatFileUploadResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static io.lettuce.core.ShutdownArgs.Builder.save;

@Service
@RequiredArgsConstructor
public class HEADChatFileService {

    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADClientsRepository headClientsRepository;
    private final HEADFileStorageService headFileStorageService;
    private final HEADJwtGenerator headJwtGenerator;
    private final HEADJobService headJobService;
    private final HEADChatMap headChatMap;

    @Transactional
    public HEADChatFileUploadResponse uploadChatFile(HEADFileMessageRequest request) throws Exception  {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        if (getUUID == null) {
            throw new HEADBadRequestException("Token Invalido");
        }
        HEADChatParticipantType userType = typeUser(getUUID);
        HEADJob jobCurrent = headJobService.findById(request.jobId());
        var uuIdUserSend = sendUuId(userType,jobCurrent);
        var folder = "chatMessage/" + userType.name() + "/" + getUUID + "/" + uuIdUserSend;
        var idUser = jobCurrent.getStaffUser().getUidUser().equals(getUUID) ? jobCurrent.getStaffUser().getIdUser() : jobCurrent.getClient().getIdUser();
        HEADFileAsset fileSaved = headFileStorageService.save(request.file(),folder, HEADOwnerType.valueOf(userType.name()), idUser, HEADCategory.MESSAGE_CHAT,null, HEADVisibility.PRIVATE, null,null,request.fileName(),null);
        return headChatMap.toResponseChatFile(fileSaved);
    }

    private HEADChatParticipantType typeUser(String uuIdUser) {

        var personalUser = personalUserRepository.findByUidUser(uuIdUser).orElse(null);
        if (personalUser != null) {
            return HEADChatParticipantType.STAFF;
        }

        var clientUser = headClientsRepository.findByUuIdUser(uuIdUser).orElse(null);
        if (clientUser != null) {
            return HEADChatParticipantType.CLIENT;
        }

        return HEADChatParticipantType.SYSTEM;
    }

    private String sendUuId(HEADChatParticipantType participantType, HEADJob jobCurrent) {
        switch (participantType) {
            case CLIENT -> {
                return jobCurrent.getStaffUser().getUidUser();
            }
            case STAFF -> {
                return jobCurrent.getClient().getUuIdUser();
            }
        }

        return "";
    }

}
