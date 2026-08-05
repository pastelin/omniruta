package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.service;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADCurrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HEADCurrentServiceService {

    // Si ya tienes repositorios de órdenes/asignaciones, inyéctalos aquí:
    // private final HEADClientOrderRepository clientOrderRepo;
    // private final HEADStaffAssignmentRepository staffAssignRepo;

    /** CLIENT: devuelve el servicio activo si existe, o null si no hay */
    public HEADCurrentService peekClientActive(Long clientId) {
        // TODO: implementar con tu modelo real de órdenes.
        // Ejemplo ficticio:
        // var order = clientOrderRepo.findActiveByClientId(clientId).orElse(null);
        // if (order == null) return null;
        // return mapOrderToCurrentService(order);
        return null;
    }

    /** STAFF: devuelve la asignación/servicio activo si existe, o null si no hay */
    public HEADCurrentService peekStaffActive(Long staffId) {
        // TODO: implementar con tu modelo real de asignaciones.
        // var asg = staffAssignRepo.findActiveByStaffId(staffId).orElse(null);
        // if (asg == null) return null;
        // return mapAssignmentToCurrentService(asg);
        return null;
    }

    // Ejemplo de mapeo (ficticio) para clientes:
    // private HEADCurrentService mapOrderToCurrentService(HEADOrder o) {
    //     String screen = switch (o.getStatus()) {
    //         case "CREATED", "CONFIRMING" -> "CLIENT.SERVICE.CONFIRMATION";
    //         case "ASSIGNED", "EN_ROUTE"   -> "CLIENT.SERVICE.TRACKING";
    //         case "IN_PROGRESS"            -> "CLIENT.SERVICE.IN_PROGRESS";
    //         case "DONE"                   -> "CLIENT.SERVICE.SUMMARY";
    //         default                       -> "CLIENT.SERVICE.TRACKING";
    //     };
    //     return new HEADCurrentService(
    //         o.getOrderId().toString(),
    //         o.getStatus(),
    //         screen,
    //         Map.of("orderId", o.getOrderId())
    //     );
    // }

    // Ejemplo de mapeo (ficticio) para staff:
    // private HEADCurrentService mapAssignmentToCurrentService(HEADAssignment a) {
    //     String screen = switch (a.getStatus()) {
    //         case "ASSIGNED"    -> "STAFF.SERVICE.ACCEPT";
    //         case "EN_ROUTE"    -> "STAFF.SERVICE.NAVIGATION";
    //         case "IN_PROGRESS" -> "STAFF.SERVICE.TASKS";
    //         case "DONE"        -> "STAFF.SERVICE.SUMMARY";
    //         default            -> "STAFF.SERVICE.TASKS";
    //     };
    //     return new HEADCurrentService(
    //         a.getAssignmentId().toString(),
    //         a.getStatus(),
    //         screen,
    //         Map.of("assignmentId", a.getAssignmentId())
    //     );
    // }
}