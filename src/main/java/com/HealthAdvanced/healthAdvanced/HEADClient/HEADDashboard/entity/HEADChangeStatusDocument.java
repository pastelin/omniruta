package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADChangeStatusDocument {
    private Long userId;
    private Integer documentId;
    private HEADDocumentStatus status;
    private Long idOccProfile;
    private String motiveNote;
}
