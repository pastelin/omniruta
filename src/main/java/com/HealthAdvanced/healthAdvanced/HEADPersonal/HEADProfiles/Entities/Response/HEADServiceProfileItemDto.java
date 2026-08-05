package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADServiceProfileItemDto {
    private Long id;                 // IdOccupationProfile (Long)
    private String title;            // nameTypeProfile (o nombre comercial)
    private String area;             // nombre del HEADOccupations (chip 1)
    private String profileType;      // tipo de perfil (chip 2) si aplica
    private List<String> tags;       // chips extra: "Domicilio", "Nocturno", etc.

    // Descripciones grises (el front las lista tal cual)
    private List<String> lines;      // "Edad: Más de 18", "Docs: 2/4 aprobados", ...

    // Estado para badges/botón
    private boolean enabled;         // backend decide si hoy puede elegirse
    private Boolean canGoOnline;     // null si no aplica aún
    private Integer docsApproved;    // para mostrar 2/4
    private Integer docsRequired;

    private String iconKey;             // clave de icono en front ("nurse","care",...)
    private boolean preselected;     // ya seleccionado por el usuario
}
