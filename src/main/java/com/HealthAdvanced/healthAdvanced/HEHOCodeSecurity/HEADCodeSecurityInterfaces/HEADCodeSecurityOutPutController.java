package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.HEADCodeSecurityInterfaces;

import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public interface HEADCodeSecurityOutPutController {
    public ResponseEntity<HEADCodeSecurityResponse> getCodeSecurity(ResponseEntity<HEADCodeSecurityResponse> response);
}
