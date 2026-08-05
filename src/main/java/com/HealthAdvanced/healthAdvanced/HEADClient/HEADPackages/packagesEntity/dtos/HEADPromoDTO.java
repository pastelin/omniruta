package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADPromoDTO {
    private String label;          // ej. "10% OFF", "Oferta", "Nuevo"
    private Integer percent;       // ej. 10 (null si solo es badge o texto)
    private String notes;          // mensaje opcional para mostrar debajo del badge
    private Integer priority;      // prioridad de la promo para resolver conflictos
}
