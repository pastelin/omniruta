package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces;

public interface HEADPrescriptionRangeView {
    Long getPrescriptionId();
    java.time.LocalDate getStartDate();
    java.time.LocalDate getEndDate();
}