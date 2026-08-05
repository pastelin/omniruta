package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADBulkRequirementsResponse {
    private List<HEADRequirementsByProfileResponse> profiles;
}