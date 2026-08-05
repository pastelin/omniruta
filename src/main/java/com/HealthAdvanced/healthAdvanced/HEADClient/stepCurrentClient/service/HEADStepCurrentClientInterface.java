package com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;

public interface HEADStepCurrentClientInterface {
    HEADStatusResponseDTO statusClient(Long clientId);
    void clientCompleteSub(Long clientId, String parentStepName, String subStepName);
    void clientCompleteStep(Long clientId, String stepName);
    HEADStepSubCatalogue getStepSubNext(String stepName, String subStepName);
    static final String CLIENT = "CLIENT";
}
