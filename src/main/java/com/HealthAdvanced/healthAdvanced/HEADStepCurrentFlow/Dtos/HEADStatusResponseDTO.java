package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos;

import java.util.List;

public record HEADStatusResponseDTO(boolean doneAll, HEADNextDTO next, List<HEADStepChecklistDTO> checklist) {
    public HEADStatusResponseDTO withChangeScreenFlow(String screenFlow) {
        return new HEADStatusResponseDTO(this.doneAll, this.next.nextCurrentFlow(screenFlow),this.checklist);
    }
}
