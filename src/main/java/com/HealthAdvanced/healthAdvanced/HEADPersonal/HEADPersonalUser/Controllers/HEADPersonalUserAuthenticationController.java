package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.Controllers;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADGoogleAuthRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.HEADWSRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpResendRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADUserOrPersonalRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADPersonalService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADPersonalWithGoogleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personalAuthentication")
@RequiredArgsConstructor
public class HEADPersonalUserAuthenticationController implements IHEADPersonalUserAuthenticationController {

    private final HEADPersonalService personalService;
    private final HEADPersonalWithGoogleService personalWithGoogleService;

    @PostMapping("/otp/verify")
    public ResponseEntity<?> otpVerifyPersonal(@RequestBody HEADVerifyRequest body) {
           return ResponseEntity.ok(personalService.otpVerifyPersonal(body));
    }

    @PostMapping("/otp/start")
    public ResponseEntity<?> otpStartPersonal(@RequestBody HEADOtpRequest body) {
       return personalService.otpStartPersonal(body);
    }

    @PostMapping("/registerUserPersonal")
    public ResponseEntity<?> registerUsersOrPersonal(@RequestBody HEADUserOrPersonalRequestDto body) {
        return personalService.savePersonal(body);
    }

    @PostMapping("/loginPersonal")
    public ResponseEntity<?> loginPersonal(@RequestBody HEADLoginPasswordRequest body) {
        return personalService.loginPersonal(body);
    }

   /*@PostMapping("/resetPassword")
    public ResponseEntity<HEADApiResponse<HEADSuccessResetPassword>> headResetPassword(
            @Valid @RequestBody HEADWSRequest<HEADResetPassword> req
    ) {
        return personalService.resetPasswordStaff(req.transaction());
    }*/

    @PostMapping("/otp/resend")
    public ResponseEntity<?> otpResend(@RequestBody HEADOtpResendRequest request) {
        return personalService.resendCode(request);
    }

    /** Debe llamarse justo al mostrar la pantalla de éxito del registro (cierra SUCCESS_REGISTER). */
    @GetMapping("/registerFinal")
    public ResponseEntity<?> registerFinal() {
        return personalService.successRegisterStaff();
    }

    @PostMapping("/google")
    public ResponseEntity<HEADApiResponse<HEADStaffRegisterResponseDto>> registerWithGoogle(
            @RequestBody HEADGoogleAuthRequest request
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(personalWithGoogleService.registerWithGoogle(request)));
    }
}

