package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.map;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADGetUserProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response.HEADMedicalInfoResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientEmergencyContactRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientMedicalInfoRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADMedicalContactEmergencyMap {
    private final HEADClientMedicalInfoRepository medicalInfoRepo;
    private final HEADClientEmergencyContactRepository emergencyRepo;
    private final HEADFileAssetRepository repo;

    public HEADMedicalInfoResponse.User mapUserAccount(HEADClients client) {
        var clientImage = repo.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT, client.getIdUser(), HEADCategory.AVATAR).orElse(null);
        String dob = client.getFechaNacimiento() != null ? client.getFechaNacimiento().toString() : null;
        return new HEADMedicalInfoResponse.User(
                client.getNombre(),
                client.getAPaterno(),
                client.getEmail(),
                client.getTelefono(),
                clientImage != null ? clientImage.getUrl() : null,
                dob,
                client.getIdSexUser() != null ? client.getIdSexUser().getTypeSex() : null
        );
    }

    public HEADMedicalInfoResponse.MedicalInfo mapMedicalInfo(long clientId) {
        var medical = medicalInfoRepo.findByClient_IdUser(clientId).orElse(null);

        return (medical == null) ? null :
                new HEADMedicalInfoResponse.MedicalInfo(
                        medical.getBloodType(),
                        medical.getWeightKg(),
                        medical.getHeightCm()
                );
    }

    public HEADMedicalInfoResponse.EmergencyContact mapEmergencyContact(long clientId) {
        var emergency = emergencyRepo.findByClient_IdUser(clientId).orElse(null);

        return (emergency == null) ? null :
                new HEADMedicalInfoResponse.EmergencyContact(
                        emergency.getFullName(),
                        emergency.getPhone(),
                        emergency.getRelationship()
                );
    }

    public List<HEADMedicalInfoResponse.GendersList> mapGenderList(List<HEADSexUser> genderList) {
        return genderList.stream().map(gender -> new HEADMedicalInfoResponse.GendersList(gender.getTypeSex(), gender.getIdSexUser())).toList();
    }
}
