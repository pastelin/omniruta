package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.request;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;

public record HEADUpdateDoseRequest(HEADDoseStatus status) {
}
