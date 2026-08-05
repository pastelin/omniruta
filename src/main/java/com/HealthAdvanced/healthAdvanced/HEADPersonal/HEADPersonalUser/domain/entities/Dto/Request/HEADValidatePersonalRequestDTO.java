package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADValidatePersonalRequestDTO {
    @Column(nullable = false, unique = true)
    @NotBlank(message = "El correo electronico no debe ir vacío")
    private String emailPersonal;
}
