package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response;

public record HEADErrorPrescriptionDto(String message) {
    public static HEADErrorPrescriptionDto mapError(String message) {
        return new HEADErrorPrescriptionDto(
                message
        );
    }
}
