package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.DocumentsDtos;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADVerificationEvent {
    private boolean canGoOnline;
    private HEADDocumentStatus status;         // p.ej. "APPROVED", "PENDING", "REJECTED"
    private HEADNextDTO nextStep;
}