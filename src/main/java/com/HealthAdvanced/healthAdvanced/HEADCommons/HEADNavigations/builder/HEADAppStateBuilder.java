package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder;

import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository.HEADStepCurrentClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.policy.HEADNavigationPolicy;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.service.HEADCurrentServiceService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepCurrentPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADStepCurrentPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADCurrentService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories.HEADStepCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepCurrentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADAppStateBuilder {

    private final HEADCurrentServiceService currentServiceService;
    private final HEADNavigationPolicy navigationPolicy;
    private final HEADStepCatalogueRepository catRepo;
    private final HEADStepCurrentClientRepository stepClientRepo;
    private final HEADStepCurrentPersonalRepository stepStaffRepo;

    public HEADAppStateDTO buildForClient(HEADClients c, HEADStatusResponseDTO stepStatus) {
        boolean registrationDone = isRegistrationDone("CLIENT", c.getIdUser());
        HEADCurrentService active = currentServiceService.peekClientActive(c.getUuIdUser());
        String role = normalizeRole(c.getRoles());
        String goTo = navigationPolicy.computeGoToScreen("CLIENT", registrationDone, stepStatus, active);

        return new HEADAppStateDTO(
                c.getUuIdUser(),
                c.getIdUser(),
                "CLIENT",
                role,
                registrationDone,
                stepStatus,
                active != null,
                active != null ? active.serviceId() : null,
                active != null ? active.status() : null,
                active != null ? active.screenFlow() : null,
                goTo,
                active != null ? active.screenParams() : Map.of()
        );
    }

    public HEADAppStateDTO buildForStaff(HEADPersonalUser s, HEADStatusResponseDTO stepStatus) {
        boolean registrationDone = isRegistrationDone("STAFF", s.getIdUser());
        HEADCurrentService active = currentServiceService.peekStaffActive(s.getUidUser());
        String role = normalizeRole(s.getRoles());
        String goTo = navigationPolicy.computeGoToScreen("STAFF", registrationDone, stepStatus, active);

        return new HEADAppStateDTO(
                s.getUidUser(),
                s.getIdUser(),
                "STAFF",
                role,
                registrationDone,
                stepStatus,
                active != null,
                active != null ? active.serviceId() : null,
                active != null ? active.status() : null,
                active != null ? active.screenFlow() : null,
                goTo,
                active != null ? active.screenParams() : Map.of()
        );
    }

    /** true si todos los pasos 'required' del catálogo están completos */
    private boolean isRegistrationDone(String flow, Long userId) {
        var catalog = catRepo.findByTypeFlowOrderByOrderNoAsc(flow);
        if ("CLIENT".equals(flow)) {
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
