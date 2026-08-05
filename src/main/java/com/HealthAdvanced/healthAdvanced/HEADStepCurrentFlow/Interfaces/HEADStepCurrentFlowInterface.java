package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Interfaces;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface HEADStepCurrentFlowInterface {
    HEADStatusResponseDTO computeStatusFunctional(
            Boolean isClient,
            List<HEADStepCurrentCatalogue> steps,
            Map<Long, Boolean> stepDoneMap,
            Function<Long, List<HEADStepSubCatalogue>> loadSubs,
            Predicate<Long> isSubDone
    );
}
