package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStepCurrentPersonalResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepCurrentPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepSubStatusPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStepSubStatusPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Interfaces.HEADStepCurrentFlowInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStepCurrentPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepSubCatalogueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class HEADStepCurrentPersonalService implements HEADStepCurrentPersonalInterface {
    @Autowired
    private HEADStepCurrentPersonalRepository perRepo;
    @Autowired
    private HEADStepSubStatusPersonalRepository subPerRepo;
    @Autowired
    private HEADStepCatalogueRepository catRepo;
    @Autowired
    private HEADStepSubCatalogueRepository subRepo;
    @Autowired
    private HEADStepCurrentFlowInterface headStepCurrentFlowInterface;
    @Autowired
    private HEADPersonalUserRepository headPersonalUserRepository;

    static final String STAFF  = "STAFF";

    @Override
    public HEADStatusResponseDTO statusStaff(Long staffId) {
        List<HEADStepCurrentCatalogue> steps =
                catRepo.findByTypeFlowOrderByOrderNoAsc(STAFF);

        Map<Long, Boolean> stepDoneMap =
                perRepo.findByIdPersonalUser_IdUser(staffId).stream()
                        .collect(Collectors.toMap(
                                r -> r.getIdStepCatalogue().getIdCatalogue(),
                                r -> Boolean.TRUE.equals(r.getIsCompleteSteps())
                        ));

        return headStepCurrentFlowInterface.computeStatusFunctional(
                false,
                steps,
                stepDoneMap,
                parentId -> subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parentId),
                subId -> subPerRepo.existsByIdPersonalUser_IdUserAndSub_IdSubAndIsCompleteTrue(staffId, subId)
        );
    }

    @Override
    @Transactional
    public void staffCompleteSub(Long staffId, String parentStepName, String subStepName) {
        // 1) Paso padre (p. ej. REGISTER) y sus sub-pasos ordenados
        var parent = catRepo.findByTypeFlowAndStepName(STAFF, parentStepName)
                .orElseThrow(() -> new HEADBadRequestException("Paso padre inválido: " + parentStepName));

        var subs = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parent.getIdCatalogue());
        var target = subs.stream()
                .filter(s -> s.getSubStepName().equals(subStepName))
                .findFirst()
                .orElseThrow(() -> new HEADBadRequestException("Sub-paso inválido: " + subStepName));

        // 2) Sub-pasos ya completos para el staff (una sola consulta, evitamos exists repetidos)
        var doneSet = subPerRepo.findByIdPersonalUser_IdUser(staffId).stream()
                .filter(HEADStepSubStatusPersonal::getIsComplete)
                .map(s -> s.getSub().getIdSub())
                .collect(java.util.stream.Collectors.toSet());

        // 3) Validar prerrequisitos: todos los requeridos anteriores al target deben estar en doneSet
        boolean prevMissing = subs.stream()
                .filter(s -> Boolean.TRUE.equals(s.getRequired()) && s.getOrderNo() < target.getOrderNo())
                .anyMatch(s -> !doneSet.contains(s.getIdSub()));
        if (prevMissing) {
            throw new HEADBadRequestException("Prerrequisito de sub-paso no cumplido antes de: " + subStepName);
        }

        // 4) Upsert idempotente del sub-paso target
        var rel = subPerRepo.findByIdPersonalUser_IdUserAndSub_IdSub(staffId, target.getIdSub())
                .orElseGet(() -> {
                    var r = new HEADStepSubStatusPersonal();
                    var u = new HEADPersonalUser(); u.setIdUser(staffId);
                    r.setIdPersonalUser(u);
                    r.setSub(target);
                    return r;
                });
        if (!Boolean.TRUE.equals(rel.getIsComplete())) {
            rel.setIsComplete(true);
            subPerRepo.save(rel);
            doneSet.add(target.getIdSub()); // reflejar en memoria para el siguiente cálculo
        }

        // 5) ¿Todos los sub-pasos requeridos están completos? → marcar el padre DONE
        boolean allReqDone = subs.stream()
                .filter(HEADStepSubCatalogue::getRequired)
                .allMatch(s -> doneSet.contains(s.getIdSub()));

        if (allReqDone) {
            var parentRel = perRepo.findByIdPersonalUser_IdUserAndIdStepCatalogue_IdCatalogue(staffId, parent.getIdCatalogue())
                    .orElseGet(() -> {
                        var r = new HEADStepCurrentPersonal();
                        var u = new HEADPersonalUser(); u.setIdUser(staffId);
                        r.setIdPersonalUser(u);
                        r.setIdStepCatalogue(parent);
                        return r;
                    });
            if (!Boolean.TRUE.equals(parentRel.getIsCompleteSteps())) {
                parentRel.setIsCompleteSteps(true);
                perRepo.save(parentRel);
            }
        }

        if (isReadyForAccess(staffId)) {
            headPersonalUserRepository.promotePersonalById(staffId);
        }
    }
    @Override
    @Transactional
    public void staffCompleteStep(Long staffId, String stepName) {
        var step = catRepo.findByTypeFlowAndStepName(STAFF, stepName)
                .orElseThrow(() -> new HEADBadRequestException("Paso inválido: " + stepName));

        var subs = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(step.getIdCatalogue());
        if (!subs.isEmpty()) throw new HEADBadRequestException("El paso es compuesto; usa staffCompleteSub().");

        var allSteps = catRepo.findByTypeFlowOrderByOrderNoAsc(STAFF);
        Set<Long> donePrevIds = perRepo.findByIdPersonalUser_IdUser(staffId).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCompleteSteps()))
                .map(r -> r.getIdStepCatalogue().getIdCatalogue())
                .collect(Collectors.toSet());

        boolean missingPrev = allSteps.stream()
                .takeWhile(s -> !s.getIdCatalogue().equals(step.getIdCatalogue()))
                .filter(s -> Boolean.TRUE.equals(s.getRequired()))
                .anyMatch(s -> !donePrevIds.contains(s.getIdCatalogue()));
        if (missingPrev) throw new HEADBadRequestException("Faltan prerrequisitos antes de " + stepName);

        var rel = perRepo.findByIdPersonalUser_IdUserAndIdStepCatalogue_IdCatalogue(staffId, step.getIdCatalogue())
                .orElseGet(() -> {
                    var r = new HEADStepCurrentPersonal();
                    var u = new HEADPersonalUser(); u.setIdUser(staffId);
                    r.setIdPersonalUser(u); r.setIdStepCatalogue(step);
                    return r;
                });
        if (!Boolean.TRUE.equals(rel.getIsCompleteSteps())) {
            rel.setIsCompleteSteps(true);
            perRepo.save(rel);
        }

        if (isReadyForAccess(staffId)) {
            headPersonalUserRepository.promotePersonalById(staffId);
        }
    }

    @Override
    @Transactional
    public HEADStepSubCatalogue getStepSubNext(String stepName, String subStepName) {
        var parent = catRepo.findByTypeFlowAndStepName(STAFF, stepName).orElseThrow();
        var subs   = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parent.getIdCatalogue());
        return subs.stream().filter(step -> step.getSubStepName().equals(subStepName)).findFirst().orElse(new HEADStepSubCatalogue());
    }

    private boolean isReadyForAccess(Long staffId) {
        // 1) catálogo completo de pasos STAFF
        List<HEADStepCurrentCatalogue> steps = catRepo.findByTypeFlowOrderByOrderNoAsc(STAFF);

        // 2) mapa de pasos padre completados por el staff
        Map<Long, Boolean> stepDoneMap = perRepo.findByIdPersonalUser_IdUser(staffId).stream()
                .collect(Collectors.toMap(
                        r -> r.getIdStepCatalogue().getIdCatalogue(),
                        r -> Boolean.TRUE.equals(r.getIsCompleteSteps())
                ));

        // 3) todos los pasos requeridos deben estar completos
        return steps.stream()
                .filter(HEADStepCurrentCatalogue::getRequired)
                .allMatch(s -> Boolean.TRUE.equals(stepDoneMap.get(s.getIdCatalogue())));
    }
}
