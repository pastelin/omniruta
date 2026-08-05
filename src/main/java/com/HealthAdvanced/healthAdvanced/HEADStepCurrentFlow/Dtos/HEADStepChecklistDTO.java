package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos;

import java.util.List;

public record HEADStepChecklistDTO(String stepName, boolean required, boolean done, String screenFlow,
                                  List<HEADSubChecklistDTO> subChecklist) {}
