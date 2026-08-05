package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.Controllers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffMenuProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings.HEADGetStaffMenuProfileService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADCertificationResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADMyProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADUpdateMyProfileRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADUpsertCertificationRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.*;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEADFileStorageService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadClientRequest;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadStaffRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff/profile")
public class HEADMyProfileController {

    private final HEADGetMyProfileService getMyProfileService;
    private final HEADUpdateMyProfileService updateMyProfileService;
    private final HEADAddCertificationService addCertificationService;
    private final HEADUpdateCertificationService updateCertificationService;
    private final HEADDeleteCertificationService deleteCertificationService;
    private final HEADFileStorageService headFileStorageService;
    private final HEADGetStaffMenuProfileService getStaffMenuProfileService;

    @GetMapping("/me")
    public ResponseEntity<HEADApiResponse<HEADMyProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                HEADApiResponse.ok(getMyProfileService.execute())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<HEADApiResponse<String>> updateMyProfile(
            @RequestBody HEADUpdateMyProfileRequest request
    ) {
        updateMyProfileService.execute(request);
        return ResponseEntity.ok(HEADApiResponse.ok("Perfil actualizado correctamente"));
    }

    @PostMapping("/certifications")
    public ResponseEntity<HEADApiResponse<HEADCertificationResponse>> addCertification(
            @RequestBody HEADUpsertCertificationRequest request
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(addCertificationService.execute(request))
        );
    }

    @PutMapping("/certifications/{certificationId}")
    public ResponseEntity<HEADApiResponse<HEADCertificationResponse>> updateCertification(
            @PathVariable Long certificationId,
            @RequestBody HEADUpsertCertificationRequest request
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(updateCertificationService.execute(certificationId, request))
        );
    }

    @DeleteMapping("/certifications/{certificationId}")
    public ResponseEntity<HEADApiResponse<String>> deleteCertification(
            @PathVariable Long certificationId
    ) {
        deleteCertificationService.execute(certificationId);
        return ResponseEntity.ok(HEADApiResponse.ok("Certificación eliminada correctamente"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStaff(@ModelAttribute HEADUploadClientRequest headUploadStaffRequest) {
        return ResponseEntity.ok(HEADApiResponse.ok(headFileStorageService.uploadStaffAvatar(headUploadStaffRequest)));
    }

    @GetMapping("/menu-profile")
    public ResponseEntity<HEADApiResponse<HEADStaffMenuProfileResponse>> getMenuProfile() {
        return ResponseEntity.ok(
                HEADApiResponse.ok(getStaffMenuProfileService.execute())
        );
    }
}
