package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos;

public record HEADNextDTO(boolean isComposite, String stepName, String subStepName, String screenFlow) {
    public HEADNextDTO nextCurrentFlow(String screenFlow) {
    return new HEADNextDTO(
            this.isComposite,
            this.stepName,
            this.subStepName,
            screenFlow);
    }
}
