package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.mapers;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADAccountHomeResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADGetUserProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientEmergencyContactRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientMedicalInfoRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffReviewRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADMapAccount {


    private final HEADFileAssetRepository repo;
    private final HEADStaffReviewRepository reviewRepo;
    private final HEADJobRepository jobRepo;


    public HEADAccountHomeResponse.User mapUserAccount(HEADClients client) {
        var clientImage = repo.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT, client.getIdUser(), HEADCategory.AVATAR).orElse(null);
        return new HEADAccountHomeResponse.User(
                client.getNombre() + " " + client.getAPaterno(),
                client.getEmail(),
                client.getTelefono(),
                clientImage != null ? clientImage.getUrl() : null,
                client.getCreatedAt().toLocalDate().toString(),
                "Basico"
        );
    }

    private String safe(String s) { return (s == null) ? "" : s; }

    private String buildClientFullName(HEADClients c) {
        String n = safe(c.getNombre());
        String p = safe(c.getAPaterno());
        return (n + " " + p).replaceAll("\\s+", " ").trim();
    }

}
