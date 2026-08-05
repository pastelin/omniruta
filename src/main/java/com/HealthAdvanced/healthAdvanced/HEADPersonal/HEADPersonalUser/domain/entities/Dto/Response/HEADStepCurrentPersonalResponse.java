package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

public class HEADStepCurrentPersonalResponse {
    private Long idStepCurrent;
    private String StepCurrentName;
    private Boolean isCompleteRegister;

    public Long getIdStepCurrent() {
        return idStepCurrent;
    }

    public void setIdStepCurrent(Long idStepCurrent) {
        this.idStepCurrent = idStepCurrent;
    }

    public String getStepCurrentName() {
        return StepCurrentName;
    }

    public void setStepCurrentName(String stepCurrentName) {
        StepCurrentName = stepCurrentName;
    }

    public Boolean getCompleteRegister() {
        return isCompleteRegister;
    }

    public void setCompleteRegister(Boolean completeRegister) {
        isCompleteRegister = completeRegister;
    }
}
