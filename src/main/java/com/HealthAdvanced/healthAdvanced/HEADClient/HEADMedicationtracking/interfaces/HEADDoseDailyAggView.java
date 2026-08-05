package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces;

import java.time.LocalDate;

public interface HEADDoseDailyAggView {
    LocalDate getDoseDate();
    long getTotal();
    long getTaken();
    long getPending();
}
