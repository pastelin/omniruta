package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.dto.HEADStaffProfileLite;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADStaffActiveCurrent {
    private Long idPersonalUser;       // el ID real del staff
    private String uuidUser;
    private Long idProfileUser;     // perfil (ej. Auxiliar, General, etc.)
    private String profileStaff;       // nombre del perfil
    private String profileOccupation;  // ocupación principal (Enfermería, Médico...)

    private double latitude;           // coordenadas para el mapa
    private double longitude;

    private double distanceKm;         // distancia real calculada en back
    private Integer etaMinutes;        // opcional: tiempo estimado en minutos
    private List<HEADStaffProfileLite> Profiles;
    private List<Long> MatchedProfileIds;
}