package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.Controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADUserOrPersonalRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADModelEmail;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADPhoneNumberRequest;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface IHEADPersonalUserAuthenticationController {
    ResponseEntity<?> otpVerifyPersonal(@RequestBody HEADVerifyRequest otpRequest);
    ResponseEntity<?> otpStartPersonal(@RequestBody HEADOtpRequest headOtpRequest);
    ResponseEntity<?> registerUsersOrPersonal(@RequestBody HEADUserOrPersonalRequestDto headUserOrPersonalRequestDto) throws HEADErrorMessageClient;
    ResponseEntity<?> loginPersonal(@RequestBody HEADLoginPasswordRequest body);
}
