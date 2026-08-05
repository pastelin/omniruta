package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.*;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Interfaces.HEADStepCurrentFlowInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class HEADStepCurrentFlowService implements HEADStepCurrentFlowInterface {
    @Override
    public HEADStatusResponseDTO computeStatusFunctional(
            Boolean isClient,
            List<HEADStepCurrentCatalogue> steps,                    // ya ordenados por orderNo
            Map<Long, Boolean> stepDoneMap,                      // idCatalogue -> done
            Function<Long, List<HEADStepSubCatalogue>> loadSubs, // parentId -> substeps ordenados
            Predicate<Long> isSubDone                            // subId -> done
    ) {
        // 1) Construye checklist paso a paso (y sub-pasos si hay)
        List<HEADStepChecklistDTO> checklist = steps.stream().map(step -> {
            List<HEADStepSubCatalogue> subs = loadSubs.apply(step.getIdCatalogue());

            if (subs.isEmpty()) {
                boolean done = Boolean.TRUE.equals(stepDoneMap.get(step.getIdCatalogue()));
                return new HEADStepChecklistDTO(step.getStepName(), step.getRequired(), done, step.getScreenFlow(), null);
            }

            // Sub-checklist
            List<HEADSubChecklistDTO> subList = subs.stream()
                    .map(sub -> new HEADSubChecklistDTO(
                            sub.getSubStepName(),
                            sub.getRequired(),
                            isSubDone.test(sub.getIdSub()),
                            sub.getScreenFlow()
                    ))
                    .collect(Collectors.toList());

            boolean allReqDone = subList.stream()
                    .filter(HEADSubChecklistDTO::required)
                    .allMatch(HEADSubChecklistDTO::done);

            boolean parentDone = Boolean.TRUE.equals(stepDoneMap.get(step.getIdCatalogue())) || allReqDone;

            return new HEADStepChecklistDTO(step.getStepName(), step.getRequired(), parentDone, step.getScreenFlow(), subList);
        }).collect(Collectors.toList());

        // 2) ¿todo lo requerido está completo?
        boolean doneAll = checklist.stream()
                .filter(HEADStepChecklistDTO::required)
                .allMatch(HEADStepChecklistDTO::done);

        var screenFlow = isClient ? "NavBarAppHome" : "HomeMap";
        // 3) Calcula el siguiente:
        //    - primer paso requerido no done
        //    - si es compuesto, primer sub requerido no done
        HEADNextDTO next = checklist.stream()
                .filter(HEADStepChecklistDTO::required)
                .filter(sc -> !sc.done())
                .findFirst()
                .map(sc -> {
                    var subs = sc.subChecklist();
                    if (subs != null && !subs.isEmpty()) {
                        return subs.stream()
                                .filter(HEADSubChecklistDTO::required)
                                .filter(s -> !s.done())
                                .findFirst()
                                .map(s -> new HEADNextDTO(true, sc.stepName(), s.subStepName(), s.screenFlow()))
                                .orElse(new HEADNextDTO(true, sc.stepName(), null, sc.screenFlow())); // fallback
                    }
                    return new HEADNextDTO(false, sc.stepName(), null, sc.screenFlow());
                })
                .orElse(new HEADNextDTO(false, "DONE", null, screenFlow));

        return new HEADStatusResponseDTO(doneAll, next, checklist);
    }

    public HEADCurrentService peekClientActive(Long clientId) {
        // TODO: Consulta orden/servicio activo del cliente si existe; si no, retorna null
        return null;
    }
    public HEADCurrentService peekStaffActive(Long staffId) {
        // TODO: Consulta asignación activa del staff si existe; si no, retorna null
        return null;
    }
}
