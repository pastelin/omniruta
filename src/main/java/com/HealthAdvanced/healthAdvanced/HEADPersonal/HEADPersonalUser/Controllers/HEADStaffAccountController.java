package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.Controllers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADDeleteStaffAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class HEADStaffAccountController {

    private final HEADDeleteStaffAccountService deleteAccountService;

    @DeleteMapping("/me")
    public ResponseEntity<HEADApiResponse<Boolean>> deleteMyAccount() {
        deleteAccountService.deleteStaffAccount();
        return ResponseEntity.ok(HEADApiResponse.ok(true));
    }
}
