package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.mappers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobSnapshotDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADJobMapper {

    private final HEADStaffStateStore stateStore;
    // Debes inyectar aquí el servicio que calcula el ETA (ruta) si lo tienes,
    // por ejemplo: private final RoutingService routingService;

    /**
     * Mapea el estado actual del Job y del Staff al DTO de actualización del cliente.
     * @param job La entidad HEADJob (debe tener staffUser cargado).
     * @return HEADClientUpdateDto
     */
    public HEADClientUpdateDto mapToClientUpdateDto(HEADJob job) {

        HEADPersonalUser staff = job.getStaffUser();

        // --- 1. Obtener Datos en Tiempo Real (Fast Data) ---

        // Si no hay staff asignado, o si el staff es nulo (ej. Job CANCELLED/EXPIRED antes de la oferta),
        // solo se puede enviar el estado del Job, pero no la ubicación/driver info.
        HEADStaffStateDto staffState = (staff != null)
                ? stateStore.get(staff.getUidUser())
                : null;

        Double driverLat = staffState != null ? staffState.lat() : null;
        Double driverLng = staffState != null ? staffState.lng() : null;

        // --- 2. Obtener Info Estática del Conductor (Datos Transaccionales) ---

        String driverName = (staff != null) ? staff.getNombre() + " " + staff.getAPaterno() : null; // Asumiendo getFullName()
        // Estos campos provendrían de una relación Vehicle dentro de HEADPersonalUser (simulación):
        String vehicleModel = (staff != null) ? staff.getOccupationLinks().stream().map(s -> s.getIdOccupationProfile().getNameTypeProfile()).collect(Collectors.joining(",")) : null;
        String vehiclePlate = (staff != null) ? staff.getNombre() : null;

        // --- 3. Calcular ETA (Tiempo Estimado de Llegada) ---
        Integer etaSeconds = null;

        // Solo calcular ETA si el conductor está asignado y en ruta o aceptado
        if (job.getState() == HEADJobState.EN_ROUTE || job.getState() == HEADJobState.ACCEPTED) {

            // Lógica de cálculo de ruta (simulación)
            if (driverLat != null && job.getClientLat() != null) {
                // etaSeconds = routingService.calculateEta(driverLat, driverLng, job.getClientLat(), job.getClientLng());
                etaSeconds = 300; // Ejemplo: 5 minutos (300 segundos)
            }
        }

        // --- 4. Mapear al DTO ---

        return new HEADClientUpdateDto(
                job.getId(),

                // Estado y Mensaje
                job.getState().name(),
                getMessageForState(job.getState()),

                // Ubicación en tiempo real
                driverLat,
                driverLng,

                // Información del Conductor
                driverName,
                (staff != null) ? staff.getNombre() : null, // Asumiendo getPhotoUrl()
                vehicleModel,
                vehiclePlate,

                // Tiempos estimados
                etaSeconds
        );
    }

    // --- Métodos de Ayuda (Lógica de Negocio) ---

    /**
     * Traduce el estado interno del Job a un mensaje legible por el cliente.
     */
    private String getMessageForState(HEADJobState state) {
        return switch (state) {
            case OFFERED, REJECTED, EXPIRED -> "Estamos buscando un conductor para su servicio.";
            case ACCEPTED -> "¡Conductor asignado! Preparándose para la recogida.";
            case EN_ROUTE -> "Su conductor está en camino.";
            case ARRIVED -> "Su conductor ha llegado al punto de recogida.";
            case STARTED -> "Viaje en curso. Disfrute su servicio.";
            case COMPLETED -> "Servicio finalizado. Gracias por viajar con nosotros.";
            case CANCELLED, WITHDRAWN -> "El servicio ha sido cancelado.";
            default -> "Actualización de estado...";
        };
    }
}

