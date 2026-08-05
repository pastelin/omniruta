package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADGetUserProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientEmergencyContact;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientMedicalInfo;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.map.HEADMedicalContactEmergencyMap;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.request.HEADUpsertProfileRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response.HEADMedicalInfoResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientEmergencyContactRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientMedicalInfoRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADSexUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HEADAccountInfoService {

    private final HEADClientsRepository clientsRepository;
    private final HEADJwtGenerator headJwtGenerator;
    private final HEADMedicalContactEmergencyMap map;
    private final HEADClientMedicalInfoRepository medicalInfoRepo;
    private final HEADClientEmergencyContactRepository emergencyRepo;
    private final IHEADSexUserRepository sexUserRepository;

    public HEADMedicalInfoResponse getProfile() {
        String uuid = headJwtGenerator.getUserNamePersonalUser();

        HEADClients client = clientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Client not found for uuid: " + uuid));

        long clientId = client.getIdUser();

        var user = map.mapUserAccount(client);
        var medicalInfo = map.mapMedicalInfo(clientId);
        var emergencyContact = map.mapEmergencyContact(clientId);
        var genderList = sexUserRepository.findAll();
        var genderMap = map.mapGenderList(genderList);

        return new HEADMedicalInfoResponse(
                user,
                medicalInfo,
                emergencyContact,
                genderMap
        );
    }

    @Transactional
    public HEADMedicalInfoResponse upsertProfile(HEADUpsertProfileRequest request) {
        String uuid = headJwtGenerator.getUserNamePersonalUser();

        HEADClients client = clientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Client not found for uuid: " + uuid));

        // -------- 1. Actualizar datos base del cliente --------
        client.setNombre(request.nombre());
        client.setAPaterno(request.apellidoPaterno());
        if (request.numberPhone() != null && (client.getTelefono() == null || client.getTelefono().isBlank())) {
            client.setTelefono(request.numberPhone());
        }
        if (request.sexUserId() != null) {
            HEADSexUser sexUser = sexUserRepository.findById(request.sexUserId())
                    .orElseThrow(() -> new HEADBadRequestException("Favor de ingresar el genero: " + request.sexUserId()));
            client.setIdSexUser(sexUser);
        }

        clientsRepository.save(client);

        // -------- 2. Upsert medical info --------
        HEADClientMedicalInfo medicalInfo = medicalInfoRepo.findByClient_IdUser(client.getIdUser())
                .orElseGet(() -> {
                    HEADClientMedicalInfo m = new HEADClientMedicalInfo();
                    m.setClient(client);
                    return m;
                });

        medicalInfo.setBloodType(request.bloodType());
        medicalInfo.setWeightKg(request.weightKg());
        medicalInfo.setHeightCm(request.heightCm());

        medicalInfoRepo.save(medicalInfo);

        // -------- 3. Upsert emergency contact --------
        HEADClientEmergencyContact emergencyContact = emergencyRepo.findByClient_IdUser(client.getIdUser())
                .orElseGet(() -> {
                    HEADClientEmergencyContact e = new HEADClientEmergencyContact();
                    e.setClient(client);
                    return e;
                });

        emergencyContact.setFullName(request.emergencyContactName());
        emergencyContact.setPhone(request.emergencyPhone());
        emergencyContact.setRelationship(request.emergencyRelationship());

        emergencyRepo.save(emergencyContact);

        return getProfile();
    }
}