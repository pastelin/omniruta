package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADRequirementsByProfileResponse {
    private Long occupationProfileId;
    private String occupationProfileName;
    private Boolean canGoOnline;
    private HEADNextDTO nextStep;
    private HEADBlockingDTO blocking;
    private List<HEADDocRequirementItemDTO> documents;
}
