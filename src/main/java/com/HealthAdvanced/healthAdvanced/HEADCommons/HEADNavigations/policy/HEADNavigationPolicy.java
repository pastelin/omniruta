package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.policy;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADCurrentService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class HEADNavigationPolicy {

    public static final String CLIENT_HOME = "CLIENT.HOME";
    public static final String STAFF_HOME  = "STAFF.HOME";

    /**
     * Regla central de navegación:
     * 1) Si hay servicio activo → va a la pantalla del servicio activo.
     * 2) Si NO terminó registro y hay next → va al screenFlow del siguiente sub-paso/paso.
     * 3) Si no hay nada pendiente → HOME del flow.
     */
    public String computeGoToScreen(String flow,
                                    boolean registrationDone,
                                    HEADStatusResponseDTO stepStatus,
                                    HEADCurrentService activeService) {
        // 1) Servicio activo manda
        if (activeService != null && activeService.screenFlow() != null) {
            return activeService.screenFlow();
        }
        // 2) Registro no terminado → siguiente sub-paso/pantalla del catálogo
        if (!registrationDone && stepStatus != null && stepStatus.next() != null) {
            String next = stepStatus.next().screenFlow();
            if (next != null && !next.isBlank()) return next;
        }
        // 3) Por default → HOME
        return "CLIENT".equalsIgnoreCase(flow) ? CLIENT_HOME : STAFF_HOME;
    }
}
