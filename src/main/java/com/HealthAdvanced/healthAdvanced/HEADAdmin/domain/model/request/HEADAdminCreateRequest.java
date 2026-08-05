package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.enums.HEADAdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HEADAdminCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100)
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private HEADAdminRole role;
}