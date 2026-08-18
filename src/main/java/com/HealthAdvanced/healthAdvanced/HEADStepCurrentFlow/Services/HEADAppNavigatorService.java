package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository.HEADStepCurrentClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository.HEADStepSubStatusClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service.HEADStepCurrentClientInterface;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepCurrentPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStepCurrentPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStepSubStatusPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADCurrentService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepSubCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepCurrentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADAppNavigatorService {

    private final HEADClientsRepository clientsRepo;
    private final HEADPersonalUserRepository staffRepo;
    private final HEADStepCatalogueRepository catRepo;
    private final HEADStepSubCatalogueRepository subRepo;
    private final HEADStepCurrentClientRepository stepClientRepo;
    private final HEADStepCurrentPersonalRepository stepStaffRepo;
    private final HEADStepSubStatusClientRepository subClientRepo;
    private final HEADStepSubStatusPersonalRepository subStaffRepo;
    private final HEADStepCurrentClientInterface clientSteps;
    private final HEADStepCurrentPersonalInterface staffSteps;
    private final HEADStepCurrentFlowService currentServiceService;

    private static final String FLOW_CLIENT = "CLIENT";
    private static final String FLOW_STAFF  = "STAFF";

    public HEADAppStateDTO resolveStateForUuid(String uuidUser) {
        var cOpt = clientsRepo.findByUuIdUser(uuidUser);
        if (cOpt.isPresent()) {
            var c = cOpt.get();
            var stepStatus = clientSteps.statusClient(c.getIdUser()); // ya lo tienes
            boolean regDone = isRegistrationDone(FLOW_CLIENT, c.getIdUser());
            var active = currentServiceService.peekClientActive(c.getUuIdUser());

            String role = normalizeRole(c.getRoles());
            String goTo = decideScreen(FLOW_CLIENT, regDone, stepStatus, active);

            return new HEADAppStateDTO(
                    c.getUuIdUser(), c.getIdUser(), FLOW_CLIENT, role,
                    regDone, stepStatus,
                    active != null, active != null ? active.serviceId() : null,
                    active != null ? active.status() : null,
                    active != null ? active.screenFlow() : null,
                    goTo,
                    active != null ? active.screenParams() : Map.of()
            );
        }

        var sOpt = staffRepo.findByUidUser(uuidUser);
        if (sOpt.isPresent()) {
            var s = sOpt.get();
            var stepStatus = staffSteps.statusStaff(s.getIdUser());
            boolean regDone = isRegistrationDone(FLOW_STAFF, s.getIdUser());
            var active = currentServiceService.peekStaffActive(s.getUidUser());

            String role = normalizeRole(s.getRoles());
            String goTo = decideScreen(FLOW_STAFF, regDone, stepStatus, active);

            return new HEADAppStateDTO(
                    s.getUidUser(), s.getIdUser(), FLOW_STAFF, role,
                    regDone, stepStatus,
                    active != null, active != null ? active.serviceId() : null,
                    active != null ? active.status() : null,
                    active != null ? active.screenFlow() : null,
                    goTo,
                    active != null ? active.screenParams() : Map.of()
            );
        }

        // No existe usuario
        throw new HEADBadRequestException("Usuario no encontrado para uuid: " + uuidUser);
    }

    /** true = todos los pasos 'required' del catálogo del flow están completos en stepCurrent* */
    private boolean isRegistrationDone(String flow, Long userId) {
        List<HEADStepCurrentCatalogue> catalog = catRepo.findByTypeFlowOrderByOrderNoAsc(flow);
        if (FLOW_CLIENT.equals(flow)) {
            var done = stepClientRepo.findByIdClient_IdUser(userId).stream()
                    .filter(HEADStepCurrentClient::getIsCompleteSteps)
                    .map(r -> r.getIdStepCatalogue().getIdCatalogue())
                    .collect(Collectors.toSet());
            return catalog.stream().filter(HEADStepCurrentCatalogue::getRequired)
                    .allMatch(s -> done.contains(s.getIdCatalogue()));
        } else {
            var done = stepStaffRepo.findByIdPersonalUser_IdUser(userId).stream()
                    .filter(HEADStepCurrentPersonal::getIsCompleteSteps)
                    .map(r -> r.getIdStepCatalogue().getIdCatalogue())
                    .collect(Collectors.toSet());
            return catalog.stream().filter(HEADStepCurrentCatalogue::getRequired)
                    .allMatch(s -> done.contains(s.getIdCatalogue()));
        }
    }

    /** arma la pantalla destino */
    private String decideScreen(String flow, boolean regDone, HEADStatusResponseDTO stepStatus, HEADCurrentService active) {
        if (active != null) return active.screenFlow();                     // prioriza servicio activo
        if (!regDone && stepStatus.next() != null) return stepStatus.next().screenFlow(); // siguiente sub-paso
        return flow.equals(FLOW_CLIENT) ? "CLIENT.HOME" : "STAFF.HOME";
    }

    private String normalizeRole(String rolesCsv) {
        if (rolesCsv == null) return "";
        var set = Arrays.stream(rolesCsv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        if (set.contains("ACCESS_CLIENT")) return "ACCESS_CLIENT";
        if (set.contains("REGISTER_CLIENT")) return "REGISTER_CLIENT";
        if (set.contains("ACCESS_PERSONAL")) return "ACCESS_PERSONAL";
        if (set.contains("REGISTER_PERSONAL")) return "REGISTER_PERSONAL";
        return set.stream().findFirst().orElse("");
    }
}
