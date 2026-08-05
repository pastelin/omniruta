package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.mapping;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADClientRegisterRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseInfoDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADTokenModel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.springframework.stereotype.Service;

@Service
public class HEADClientLoginMapping {
    public HEADClients clientRegisterMap(HEADClientRegisterRequestDto headClientRegisterRequestDto) {
        var headClient = new HEADClients();
        headClient.setEmail(headClientRegisterRequestDto.getEmail());
        headClient.setNombre(headClientRegisterRequestDto.getNameClient());
        headClient.setAMaterno(headClientRegisterRequestDto.getFirstName());
        headClient.setAPaterno(headClientRegisterRequestDto.getLastName());
        headClient.setTelefono(headClientRegisterRequestDto.getNumberPhone());
        headClient.setPassword(HEADCommonsUtils.setEncodeValue(headClientRegisterRequestDto.getPassword()));
        headClient.setFechaNacimiento(headClientRegisterRequestDto.getBirthDate());
        return headClient;
    }

    public HEADClientRegisterResponseInfoDto clientResponseDto(HEADClients headClients) {
        var headClientDto = new HEADClientRegisterResponseInfoDto();
        headClientDto.setNameClient(headClients.getNombre());
        headClientDto.setFirstName(headClients.getAMaterno());
        headClientDto.setLastName(headClients.getAPaterno());
        headClientDto.setBirthDate(headClients.getFechaNacimiento());
        headClientDto.setEmail(headClients.getEmail());
        headClientDto.setNumberPhone(headClients.getTelefono());
        return headClientDto;
    }

    public HEADCodeSecurityResponse securityCodeMap(HEADClients headClients,
                                                    HEADCodeSecurityResponse codeSend) {
        var headCodeSecurity = new HEADJwtUsersResponse();
        headCodeSecurity.setName(headClients.getNombre());
        headCodeSecurity.setUidUser(headClients.getUuIdUser());
        headCodeSecurity.setFirstName(headClients.getAPaterno());
        headCodeSecurity.setLastName(headClients.getAMaterno());
        headCodeSecurity.setIdUser(headClients.getIdUser());
        headCodeSecurity.setEmail(headClients.getEmail());
        headCodeSecurity.setNumberPhone(headClients.getTelefono());
        headCodeSecurity.setIsAccepted(null);
        codeSend.setHeadJwtUsersResponse(headCodeSecurity);
        return codeSend;
    }

    public HEADJwtUsersResponse clientLoginMapDto(HEADClients headClients, HEADStatusResponseDTO headStatusResponseDTO, HEADTokenModel tokenModel) {
        var userResponse = new HEADJwtUsersResponse();
        userResponse.setExpiresAt(tokenModel.expiresAt());
        userResponse.setAccessToken(tokenModel.tokenAccess());
        userResponse.setUidUser(headClients.getUuIdUser());
        userResponse.setStepCurrent(headStatusResponseDTO);
        userResponse.setEmail(headClients.getEmail());
        userResponse.setName(headClients.getNombre());
        userResponse.setFirstName(headClients.getAMaterno());
        userResponse.setLastName(headClients.getAPaterno());
        userResponse.setIsAccepted(headClients.getIsAccepted());
        userResponse.setNumberPhone(headClients.getTelefono());
        return userResponse;
    }

    public HEADSuccessResetPassword clientResetPassword(HEADStatusResponseDTO st) {
        var next = st.withChangeScreenFlow("LoginRegisterScreen");
        return new HEADSuccessResetPassword(next);
    }
}
