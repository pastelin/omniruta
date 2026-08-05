package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.servicesMap;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseInfoDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADTokenModel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADPersonalUserDTO;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADUserOrPersonalRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.*;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HEADPersonalUserMapService {

    @Autowired
    HEADStepCatalogueRepository headStepCatalogueRepository;

    @Autowired
    HEADJwtGenerator headJwtGenerator;


    public HEADPersonalUser createPersonalUser(HEADPersonalUserDTO personalUserDTO, HEADSexUser sexUser) {
        personalUserDTO.setPassword(HEADCommonsUtils.setEncodeValue(personalUserDTO.getPassword()));
        return new HEADPersonalUser(personalUserDTO, sexUser);
    }
    public  HEADPersonalUserDTO createPersonalUserDTO(HEADPersonalUser personalUser) {
        return new HEADPersonalUserDTO(personalUser);
    }

    public HEADPersonalUserDTO personalUserMapRequest(HEADUserOrPersonalRequestDto headUserOrPersonalRequestDto, String roles, String sexDesc) {
        return new HEADPersonalUserDTO(headUserOrPersonalRequestDto,roles, sexDesc);
    }
    public List<HEADPersonalUserDTO> activePersonalDTOList(List<HEADPersonalUser> listPersonalUser) {
        List<HEADPersonalUserDTO> personalUserDTOS = new ArrayList<>();
        listPersonalUser.stream().forEach(
                personalUser -> {
                    personalUserDTOS.add(createPersonalUserDTO(personalUser));
                }
        );
        return personalUserDTOS;
    }
    public HEADResponseExistUser personalExist(HEADPersonalUser personalUser) {
        HEADResponseExistUser personaIsExist = new HEADResponseExistUser();
        if(personalUser != null) {
            personaIsExist.setExistUser(personalUser.getIsEnabled());
        }
        else {
            personaIsExist.setExistUser(false);
        }
        return personaIsExist;
    }
    public HEADResponseExistUser findByPersonalEmail(HEADPersonalUser headPersonalUserDTO)
    {
        HEADResponseExistUser personalExists = new HEADResponseExistUser();
        personalExists.setExistUser(headPersonalUserDTO.getIsEnabled());
        return personalExists;
    }
    public HEHOOccupationPersonalUser occupationPersonalUser(HEADPersonalUser headPersonalUser, HEADOccupationProfile occupationProfile) {
        var occupation = new HEHOOccupationPersonalUser();
            occupation.setIdOccupationProfile(occupationProfile);
            occupation.setIdPersonalUser(headPersonalUser);
            return occupation;

    }
    public UserDetails mapUserDetails(HEHOOccupationPersonalUser occupationPersonalUser) {
        Set<String> roles = new HashSet<>();
        roles.add(occupationPersonalUser.getIdOccupationProfile().getIdOccupation().getNameOccupation());
        Set<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        var user = new User(
                occupationPersonalUser.getIdPersonalUser().getAMaterno(),
                occupationPersonalUser.getIdPersonalUser().getPassword(),
                occupationPersonalUser.getIdPersonalUser().getIsEnabled(),
                true,
                true,
                true,
                authorities
        );
        return user;
    }

    public HEADCodeSecurityResponse securityCodeMap(HEADPersonalUser headPersonalUser,
                                                    HEADCodeSecurityResponse codeSend,
                                                    HEADStatusResponseDTO headStatusResponseDTO) {
        var headCodeSecurity = new HEADJwtUsersResponse();
        headCodeSecurity.setName(headPersonalUser.getNombre());
        headCodeSecurity.setUidUser(headPersonalUser.getUidUser());
        headCodeSecurity.setFirstName(headPersonalUser.getAPaterno());
        headCodeSecurity.setLastName(headPersonalUser.getAMaterno());
        headCodeSecurity.setIdUser(headPersonalUser.getIdUser());
        headCodeSecurity.setEmail(headPersonalUser.getEmail());
        headCodeSecurity.setIsAccepted(headPersonalUser.getIsEnabled());
        headCodeSecurity.setNumberPhone(headPersonalUser.getTelefono());
        headCodeSecurity.setStepCurrent(headStatusResponseDTO);
        headCodeSecurity.setIsExistsPersonal(true);
        codeSend.setHeadJwtUsersResponse(headCodeSecurity);
        return codeSend;
    }
    public HEADJwtUsersResponse headJwtUsersResponse(HEADPersonalUserDTO headPersonalUserDTO) {
        return new HEADJwtUsersResponse(headPersonalUserDTO);
    }
    public HEADStepCurrentPersonalResponse headStepCurrentPersonalResponse(HEADStepCurrentPersonal headStepCurrentPersonal, Boolean completeRegister) {
        var stepCurrent = new HEADStepCurrentPersonalResponse();
        stepCurrent.setStepCurrentName(headStepCurrentPersonal.getIdStepCatalogue().getStepName());
        stepCurrent.setCompleteRegister(completeRegister);
        stepCurrent.setIdStepCurrent(headStepCurrentPersonal.getIdStepCurrentPersonal());
        return stepCurrent;
    }

    public HEADJwtUsersResponse headJwtUsersMap(HEADPersonalUser headPersonal, HEADStatusResponseDTO headStatusResponseDTO, HEADTokenModel tokenModel) {
        var userResponse = new HEADJwtUsersResponse();
        userResponse.setExpiresAt(tokenModel.expiresAt());
        userResponse.setAccessToken(tokenModel.tokenAccess());
        userResponse.setUidUser(headPersonal.getUidUser());
        userResponse.setStepCurrent(headStatusResponseDTO);
        userResponse.setEmail(headPersonal.getEmail());
        userResponse.setName(headPersonal.getNombre());
        userResponse.setFirstName(headPersonal.getAMaterno());
        userResponse.setLastName(headPersonal.getAPaterno());
        userResponse.setIsAccepted(headPersonal.getIsEnabled());
        userResponse.setNumberPhone(headPersonal.getTelefono());
        return userResponse;
    }

    public HEADStaffRegisterResponseDto staffRegisterResponseDto(HEADPersonalUser headPersonalUser) {
        var headStaffResponseDto = new HEADStaffRegisterResponseDto();
        var staffInfoData = staffResponseDto(headPersonalUser);
        headStaffResponseDto.setIsRegisterUser(true);
        headStaffResponseDto.setDataStaff(staffInfoData);
        return headStaffResponseDto;
    }

    private HEADStaffRegisterResponseInfoDto staffResponseDto(HEADPersonalUser headPersonalUser) {
        var headStaffDto = new HEADStaffRegisterResponseInfoDto();
        headStaffDto.setNameStaff(headPersonalUser.getNombre());
        headStaffDto.setFirstName(headPersonalUser.getAMaterno());
        headStaffDto.setLastName(headPersonalUser.getAPaterno());
        headStaffDto.setBirthDate(headPersonalUser.getFechaNacimiento());
        headStaffDto.setEmail(headPersonalUser.getEmail());
        headStaffDto.setNumberPhone(headPersonalUser.getTelefono());
        return headStaffDto;
    }

    public HEADSuccessResetPassword staffResetPassword(HEADStatusResponseDTO st) {
        var next = st.withChangeScreenFlow("LoginRegisterScreen");
        return new HEADSuccessResetPassword(next);
    }
}
