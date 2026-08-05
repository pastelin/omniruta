package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.HEADChatUserProfileDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HEADChatProfileService {

    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADClientsRepository headClientsRepository;
    private final HEADFileAssetRepository headFileAssetRepository;

    public HEADChatUserProfileDto getProfile(String uuIdUser) {

        // 1) ¿Es STAFF?
        var staff = personalUserRepository.findByUidUser(uuIdUser).orElse(null);
        if (staff != null) {
            var avatar = headFileAssetRepository
                    .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                            HEADOwnerType.STAFF,
                            staff.getIdUser(),
                            HEADCategory.AVATAR
                    )
                    .orElse(null);

            String avatarUrl = avatar != null ? avatar.getUrl() : null;

            String displayName = staff.getNombre() + " " + staff.getAPaterno();

            return new HEADChatUserProfileDto(
                    uuIdUser,
                    HEADChatParticipantType.STAFF,
                    displayName,
                    avatarUrl
            );
        }

        // 2) ¿Es CLIENT?
        var client = headClientsRepository.findByUuIdUser(uuIdUser).orElse(null);
        if (client != null) {
            var avatar = headFileAssetRepository
                    .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                            HEADOwnerType.CLIENT,
                            client.getIdUser(),
                            HEADCategory.AVATAR
                    )
                    .orElse(null);

            String avatarUrl = avatar != null ? avatar.getUrl() : null;

            String displayName = client.getNombre() + " " + client.getAPaterno();

            return new HEADChatUserProfileDto(
                    uuIdUser,
                    HEADChatParticipantType.CLIENT,
                    displayName,
                    avatarUrl
            );
        }

        // 3) Si no es ninguno, lo marcamos como SYSTEM/UNKNOWN
        return new HEADChatUserProfileDto(
                uuIdUser,
                HEADChatParticipantType.SYSTEM,
                uuIdUser,
                null
        );
    }
}

