package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums;

public enum HEADOccupationCode {
    DOCTOR("Doctor(a)"),
    NURSE("Enfermero(a)"),
    CAREGIVER("Cuidador(a)"),
    THERAPIST("Terapeuta");

    private final String labelEs;

    HEADOccupationCode(String labelEs) {
        this.labelEs = labelEs;
    }

    public String getLabelEs() {
        return labelEs;
    }
}