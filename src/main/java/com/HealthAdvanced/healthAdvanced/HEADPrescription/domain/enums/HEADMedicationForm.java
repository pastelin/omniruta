package com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums;

import lombok.Getter;

@Getter
public enum HEADMedicationForm {

    TABLET("Tableta", "💊"),
    CAPSULE("Cápsula", "💊"),
    SYRUP("Jarabe / Suspensión", "🥄"),
    INJECTION("Inyección", "💉"),
    DROPS("Gotas", "💧"),
    INHALER("Inhalador", "🌬️"),
    OINTMENT("Crema / Pomada", "🧴");

    private final String labelEs;
    private final String emoji;

    HEADMedicationForm(String labelEs, String emoji) {
        this.labelEs = labelEs;
        this.emoji = emoji;
    }

}
