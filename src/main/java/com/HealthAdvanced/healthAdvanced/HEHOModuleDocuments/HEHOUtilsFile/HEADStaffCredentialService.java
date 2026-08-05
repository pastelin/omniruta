package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADCredentialType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADStaffCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HEADStaffCredentialService {

    private final HEADStaffCredentialRepository credentialRepo;
    private final HEADPersonalUserRepository personalUserRepo;
    private final HEADOccupationsProfilesRepository occupationProfileRepo;

    @Transactional
    public HEADStaffCredential upsertLicenseNo(Long staffUserId, Long occProfileIdOrNull, String licenseNo) {
        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            throw new HEADBadRequestException("licenseNo is required");
        }

        var staff = personalUserRepo.findById(staffUserId)
                .orElseThrow(() -> new HEADBadRequestException("Staff not found: " + staffUserId));

        HEADOccupationProfile occProfile = null;
        if (occProfileIdOrNull != null) {
            occProfile = occupationProfileRepo.findById(occProfileIdOrNull)
                    .orElseThrow(() -> new HEADBadRequestException("OccupationProfile not found: " + occProfileIdOrNull));
        }

        // Buscar existente (global o por perfil)
        var existingOpt = (occProfile == null)
                ? credentialRepo.findByStaffUser_IdUserAndOccupationProfileIsNullAndCredentialType(
                staffUserId, HEADCredentialType.LICENSE_NO
        )
                : credentialRepo.findCredential(
                staffUserId, occProfile.getIdOccupationProfile(), HEADCredentialType.LICENSE_NO
        );

        var normalized = licenseNo.trim();

        HEADStaffCredential c = existingOpt.orElseGet(HEADStaffCredential::new);
        c.setStaffUser(staff);
        c.setOccupationProfile(occProfile); // null = GLOBAL
        c.setCredentialType(HEADCredentialType.LICENSE_NO);
        c.setValue(normalized);

        // si el staff lo manda, queda PENDING hasta que admin apruebe
        if (c.getStatus() == null || c.getStatus() == HEADDocumentStatus.NOT_UPLOADED) {
            c.setStatus(HEADDocumentStatus.PENDING);
        } else if (c.getStatus() == HEADDocumentStatus.REJECTED) {
            // Si estaba rechazado y lo vuelve a mandar, regresa a pending
            c.setStatus(HEADDocumentStatus.PENDING);
            c.setReviewedAt(null);
            c.setReviewedByAdminId(null);
            c.setSourceDocument(null);
        }

        c.setUpdatedAt(LocalDateTime.now());
        return credentialRepo.save(c);
    }
}
