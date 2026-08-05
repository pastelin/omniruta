package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces;

public interface HEADDoseDayAggView {
    Long getPrescriptionId();
    java.time.LocalDate getDoseDate();
    long getTotal();
    long getTaken();
}