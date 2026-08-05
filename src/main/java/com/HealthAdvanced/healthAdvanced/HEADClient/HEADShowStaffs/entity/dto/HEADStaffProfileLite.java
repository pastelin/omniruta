package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADStaffProfileLite {
    private Long idProfileUser;
    private Integer idOccupation;
    private String profileStaff;       // "Enfermero auxiliar", "Médico general", etc.
    private String profileOccupation;  // "Enfermería", "Médico", etc.
    private String imageUrl;
}
