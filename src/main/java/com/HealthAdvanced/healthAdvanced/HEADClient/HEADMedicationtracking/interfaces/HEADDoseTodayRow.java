package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;

public interface HEADDoseTodayRow {
    Long getDoseId();
    java.time.LocalTime getDoseTime();
    HEADDoseStatus getStatus();

    Long getMedicationId();
    String getMedicationName();
    String getMedicationDosage();
    HEADMedicationForm getMedForm();
}