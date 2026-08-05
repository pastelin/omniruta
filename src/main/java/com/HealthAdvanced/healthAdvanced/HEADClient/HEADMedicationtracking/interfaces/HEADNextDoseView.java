package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;

import java.time.LocalTime;

public interface HEADNextDoseView {
    Long getDoseId();
    LocalTime getDoseTime();
    Long getMedicationId();
    String getMedicationName();
    String getMedicationDosage();
    HEADMedicationForm getMedForm();
}