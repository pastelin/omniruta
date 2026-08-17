package com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository.HEADStepCurrentClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository.HEADStepSubStatusClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Interfaces.HEADStepCurrentFlowInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepSubCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepCurrentClient;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepSubStatusClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class HEADStepCurrentClientService implements HEADStepCurrentClientInterface {
    @Autowired
    private HEADStepCurrentClientRepository cliRepo;
    @Autowired
    private HEADStepCatalogueRepository catRepo;
    @Autowired
    private HEADStepSubCatalogueRepository subRepo;
    @Autowired
    private HEADStepSubStatusClientRepository subCliRepo;
    @Autowired
    private HEADStepCurrentFlowInterface headStepCurrentFlowInterface;
    @Autowired
    private HEADClientsRepository headClientsRepository;

    static final String CLIENT = "CLIENT";

    @Override
    public HEADStatusResponseDTO statusClient(Long clientId) {
        // catálogo ordenado (source of truth)
        List<HEADStepCurrentCatalogue> steps =
                catRepo.findByTypeFlowOrderByOrderNoAsc(CLIENT);

        // pasos completados por el usuario (idCatalogue -> done)
        Map<Long, Boolean> stepDoneMap =
                cliRepo.findByIdClient_IdUser(clientId).stream()
                        .collect(Collectors.toMap(
                                r -> r.getIdStepCatalogue().getIdCatalogue(),
                                r -> Boolean.TRUE.equals(r.getIsCompleteSteps())
                        ));

        return headStepCurrentFlowInterface.computeStatusFunctional(
                true,
                steps,
                stepDoneMap,
                parentId -> subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parentId),   // carga sub-pasos
                subId -> subCliRepo.existsByIdClient_IdUserAndSub_IdSubAndIsCompleteTrue(clientId, subId) // ¿sub done?
        );
    }

    @Override
    @Transactional
    public void clientCompleteSub(Long clientId, String parentStepName, String subStepName) {
        var parent = catRepo.findByTypeFlowAndStepName(CLIENT, parentStepName)
                .orElseThrow(() -> new HEADBadRequestException(
                        "Catálogo faltante: no existe stepCatalogue typeFlow=" + CLIENT + " stepName=" + parentStepName));
        var subs   = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parent.getIdCatalogue());

        var target = subs.stream()
                .filter(s -> s.getSubStepName().equals(subStepName))
                .findFirst()
                .orElseThrow(() -> new HEADBadRequestException("Substep inválido"));

        // Prerrequisitos previos requeridos:
        boolean prevMissing = subs.stream()
                .filter(s -> s.getRequired() && s.getOrderNo() < target.getOrderNo())
                .anyMatch(s -> !subCliRepo
                        .existsByIdClient_IdUserAndSub_IdSubAndIsCompleteTrue(clientId, s.getIdSub()));
        if (prevMissing) throw new HEADBadRequestException("Prerrequisito de sub-paso no cumplido.");

        // Upsert idempotente
        var rel = subCliRepo.findByIdClient_IdUserAndSub_IdSub(clientId, target.getIdSub())
                .orElseGet(() -> {
                    var r = new HEADStepSubStatusClient();
                    var c = new HEADClients(); c.setIdUser(clientId);
                    r.setIdClient(c); r.setSub(target);
                    return r;
                });
        rel.setIsComplete(true);
        subCliRepo.save(rel);

        // ¿Todos los requeridos listos? => marcar padre DONE
        boolean allReqDone = subs.stream()
                .filter(HEADStepSubCatalogue::getRequired)
                .allMatch(s -> subCliRepo
                        .existsByIdClient_IdUserAndSub_IdSubAndIsCompleteTrue(clientId, s.getIdSub()));

        if (allReqDone) {
            var parentRel = cliRepo.findByIdClient_IdUserAndIdStepCatalogue_IdCatalogue(clientId, parent.getIdCatalogue())
                    .orElseGet(() -> {
                        var r = new HEADStepCurrentClient();
                        var c = new HEADClients(); c.setIdUser(clientId);
                        r.setIdClient(c); r.setIdStepCatalogue(parent);
                        return r;
                    });
            parentRel.setIsCompleteSteps(true);
            cliRepo.save(parentRel);
        }

        if (isReadyForAccess(clientId)) {
            var rows = headClientsRepository.promoteClientById(clientId);
            log.info("[SUCCESS_REGISTER] update affected rows={}", rows);
        }

    }

    @Override
    @Transactional
    public void clientCompleteStep(Long clientId, String stepName) {
        var step = catRepo.findByTypeFlowAndStepName(CLIENT, stepName)
                .orElseThrow(() -> new HEADBadRequestException("Paso inválido: " + stepName));

        // Si es compuesto (tiene sub-pasos), se debe cerrar por sub-pasos
        var subs = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(step.getIdCatalogue());
        if (!subs.isEmpty()) throw new HEADBadRequestException("El paso es compuesto; usa clientCompleteSub().");

        // Validar prerrequisitos (todos los pasos requeridos previos en orderNo)
        var allSteps = catRepo.findByTypeFlowOrderByOrderNoAsc(CLIENT);
        Set<Long> donePrevIds = cliRepo.findByIdClient_IdUser(clientId).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCompleteSteps()))
                .map(r -> r.getIdStepCatalogue().getIdCatalogue())
                .collect(Collectors.toSet());

        boolean missingPrev = allSteps.stream()
                .takeWhile(s -> !s.getIdCatalogue().equals(step.getIdCatalogue()))
                .filter(s -> Boolean.TRUE.equals(s.getRequired()))
                .anyMatch(s -> !donePrevIds.contains(s.getIdCatalogue()));
        if (missingPrev) throw new HEADBadRequestException("Faltan prerrequisitos antes de " + stepName);

        // Upsert idempotente del paso
        var rel = cliRepo.findByIdClient_IdUserAndIdStepCatalogue_IdCatalogue(clientId, step.getIdCatalogue())
                .orElseGet(() -> {
                    var r = new HEADStepCurrentClient();
                    var c = new HEADClients(); c.setIdUser(clientId);
                    r.setIdClient(c); r.setIdStepCatalogue(step);
                    return r;
                });
        if (!Boolean.TRUE.equals(rel.getIsCompleteSteps())) {
            rel.setIsCompleteSteps(true);
            cliRepo.save(rel);
        }

        if (isReadyForAccess(clientId)) {
            var rows = headClientsRepository.promoteClientById(clientId);
            log.info("[SUCCESS_REGISTER] update rows={}", rows);
        }
    }

    @Override
    @Transactional
    public HEADStepSubCatalogue getStepSubNext(String stepName, String subStepName) {
        var parent = catRepo.findByTypeFlowAndStepName(CLIENT, stepName)
                .orElseThrow(() -> new HEADBadRequestException(
                        "Catálogo faltante: no existe stepCatalogue typeFlow=" + CLIENT + " stepName=" + stepName));
        var subs   = subRepo.findByStepParent_IdCatalogueOrderByOrderNoAsc(parent.getIdCatalogue());
        return subs.stream().filter(step -> step.getSubStepName().equals(subStepName)).findFirst().orElse(new HEADStepSubCatalogue());
    }

    private boolean isReadyForAccess(Long clientId) {
        // 1) Catálogo completo de CLIENT ordenado
        List<HEADStepCurrentCatalogue> steps =
                catRepo.findByTypeFlowOrderByOrderNoAsc(CLIENT);

        // 2) Pasos padre completos por cliente
        Map<Long, Boolean> stepDoneMap =
                cliRepo.findByIdClient_IdUser(clientId).stream()
                        .collect(Collectors.toMap(
                                r -> r.getIdStepCatalogue().getIdCatalogue(),
                                r -> Boolean.TRUE.equals(r.getIsCompleteSteps())
                        ));

        // 3) Todos los 'required' deben estar completos
        var isCompleted = steps.stream()
                .filter(HEADStepCurrentCatalogue::getRequired)
                .allMatch(s -> Boolean.TRUE.equals(stepDoneMap.get(s.getIdCatalogue())));

        log.info("[SUCCESS_REGISTER] isCompletedStep={}", isCompleted);
        return isCompleted;
    }

}
