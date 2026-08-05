package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.contracts;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpStarRes;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpVerifyRes;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;

public interface IHEADOtpService {
    HEADOtpStarRes start(HEADOtpRequest headOtpRequest,String role);
    HEADOtpVerifyRes verify(HEADVerifyRequest headVerifyRequest);
    HEADOtpStarRes resend(String txId);

}
