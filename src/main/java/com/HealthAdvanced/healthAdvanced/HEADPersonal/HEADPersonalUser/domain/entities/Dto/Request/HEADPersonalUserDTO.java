package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStepCurrentPersonalResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Data
@NoArgsConstructor
public class HEADPersonalUserDTO {
    private String uidUser;
    private String nombre;
    private String paterno;
    private String materno;
    private LocalDate fechaNacimiento;
    private String telefono;
    private Boolean isAccepted;
    private String email;
    private String password;
    private Boolean isExistsPersonal;
    private HEADStatusResponseDTO headStepCurrentPersonal;
    private String idUserSex;
    private String roles;


    public HEADPersonalUserDTO(HEADPersonalUser userPersonal) {
        this.uidUser = userPersonal.getUidUser();
        this.nombre = userPersonal.getNombre();
        this.paterno = userPersonal.getAPaterno();
        this.materno = userPersonal.getAMaterno();
        this.fechaNacimiento = userPersonal.getFechaNacimiento();
        this.telefono = userPersonal.getTelefono();
        this.email = userPersonal.getEmail();
        this.password = userPersonal.getPassword();
        this.isAccepted = userPersonal.getIsEnabled();
        this.roles = userPersonal.getRoles();
        this.idUserSex = userPersonal.getIdSexUser() != null ? userPersonal.getIdSexUser().getTypeSex() : null;
    }

    public HEADPersonalUserDTO(HEADUserOrPersonalRequestDto headUserOrPersonalRequestDto, String roles, String sexDescription) {
        this.uidUser = generatorUUID();
        this.nombre = headUserOrPersonalRequestDto.getNameStaff();
        this.paterno = headUserOrPersonalRequestDto.getFirstName();
        this.materno = headUserOrPersonalRequestDto.getLastName();
        this.fechaNacimiento = headUserOrPersonalRequestDto.getBirthDate();
        this.telefono = headUserOrPersonalRequestDto.getNumberPhone();
        this.email = headUserOrPersonalRequestDto.getEmail();
        this.password = headUserOrPersonalRequestDto.getPassword();
        this.idUserSex = sexDescription;
        this.isAccepted = false;
        this.roles = roles;
    }
}
