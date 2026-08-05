package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.HEADCodeSecurityInterfaces;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADModelEmail;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;


public interface HEADCodeSecurityInputRepository {
    Boolean sendMessage(String numberPhone, String code);
    Boolean sendMessageEmail(String email, String code);
    Boolean verifySmsCode(String numberPhone, String Code);
}
