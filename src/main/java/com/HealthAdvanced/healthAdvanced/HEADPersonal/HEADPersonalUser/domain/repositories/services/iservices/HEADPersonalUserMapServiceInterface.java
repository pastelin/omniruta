package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices;

import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpStarRes;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADPersonalUserDTO;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADUserOrPersonalRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADModelEmail;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADPhoneNumberRequest;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface HEADPersonalUserMapServiceInterface {
    HEADCodeSecurityResponse otpVerifyPersonal(HEADVerifyRequest otpRequest);
    ResponseEntity<HEADOtpStarRes> otpStartPersonal(HEADOtpRequest req);
    ResponseEntity<HEADStaffRegisterResponseDto> savePersonal(HEADUserOrPersonalRequestDto dto);
    ResponseEntity<?> loginPersonal(HEADLoginPasswordRequest req);
}
