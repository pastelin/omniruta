package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADUiFlagsDTO {
    private String hint;             // texto guía (según doc/tipo)
    private Integer maxSizeMb;       // límite de peso
    private Boolean showDelete;      // mostrar botón eliminar
    private Boolean reuploadEnabled; // permitir re-subir ahora
}