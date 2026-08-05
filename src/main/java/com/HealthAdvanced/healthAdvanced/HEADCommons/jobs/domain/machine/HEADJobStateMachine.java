package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.machine;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADInvalidJobTransitionException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelledBy;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class HEADJobStateMachine {

    // Transiciones válidas
    private static final EnumMap<HEADJobState, Set<HEADJobState>> ALLOWED =
            new EnumMap<>(HEADJobState.class);
    static {
        ALLOWED.put(HEADJobState.PENDING_ASSIGNMENT, Set.of(HEADJobState.OFFERED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.OFFERED,   Set.of(HEADJobState.ACCEPTED, HEADJobState.ACCEPTED_AWAITING_START, HEADJobState.REJECTED, HEADJobState.EXPIRED, HEADJobState.WITHDRAWN));
        ALLOWED.put(HEADJobState.ACCEPTED,  Set.of(HEADJobState.EN_ROUTE, HEADJobState.ARRIVED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.EN_ROUTE,  Set.of(HEADJobState.ARRIVED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.ARRIVED,   Set.of(HEADJobState.STARTED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.STARTED,   Set.of(HEADJobState.PAUSED, HEADJobState.COMPLETED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.PAUSED,    Set.of(HEADJobState.STARTED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.SCHEDULE_PENDING, Set.of(HEADJobState.OFFERED, HEADJobState.SCHEDULED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.SCHEDULED,        Set.of(HEADJobState.OFFERED, HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.COMPLETED, Set.of());
        ALLOWED.put(HEADJobState.CANCELLED, Set.of());
        ALLOWED.put(HEADJobState.REJECTED,  Set.of());
        ALLOWED.put(HEADJobState.EXPIRED,   Set.of(HEADJobState.CANCELLED));
        ALLOWED.put(HEADJobState.WITHDRAWN, Set.of());

    }

    public boolean canTransition(HEADJobState from, HEADJobState to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Aplica transición y actualiza timestamps de HEADJob.
     * No maneja cancel reasons; usa helpers específicos abajo.
     */
    public void apply(HEADJob job, HEADJobState next, Instant now) {
        HEADJobState from = job.getState();
        if (!canTransition(from, next)) {
            throw new HEADInvalidJobTransitionException(job.getId(), from, next, null);
        }

        // Set timestamps según destino
        switch (next) {
            case ACCEPTED -> {
                if (job.getAcceptedAt() == null) job.setAcceptedAt(now);
            }
            case EN_ROUTE -> {
                // solo estado intermedio; sin timestamp propio
            }
            case ARRIVED -> {
                if (job.getArrivedAt() == null) job.setArrivedAt(now);
            }
            case STARTED -> {
                if (job.getStartedAt() == null) job.setStartedAt(now);
            }
            case PAUSED -> { /* opcional: guardar una tabla de pausas */ }
            case COMPLETED -> {
                if (job.getCompletedAt() == null) job.setCompletedAt(now);
            }
            case CANCELLED -> {
                if (job.getCancelledAt() == null) job.setCancelledAt(now);
            }
            case REJECTED -> {
                if (job.getCancelledAt() == null) job.setCancelledAt(now); // tratamos rechazo como cierre del ciclo de oferta
            }
            case EXPIRED -> {
                if (job.getCancelledAt() == null) job.setCancelledAt(now);
            }
            case WITHDRAWN -> {
                if (job.getCancelledAt() == null) job.setCancelledAt(now);
            }
            case OFFERED -> {
                if (job.getAssignedAt() == null) job.setAssignedAt(now);
            }
        }

        job.setState(next);
    }

    /* ───────────── Helpers explícitos para casos con razón / actor ───────────── */

    public void reject(HEADJob job, Instant now, String note) {
        if (!canTransition(job.getState(), HEADJobState.REJECTED)) {
            throw new HEADInvalidJobTransitionException(job.getId(), job.getState(), HEADJobState.REJECTED, "reject()");
        }
        job.setCancelledAt(now);
        job.setCancelNote(trim255(note));
        job.setCancelledBy(HEADCancelledBy.STAFF);     // usa tu enum
        job.setCancelReason(HEADCancelReason.OTHER);   // o un enum REJECTED si lo tienes
        job.setState(HEADJobState.REJECTED);
    }

    public void expire(HEADJob job, Instant now) {
        if (!canTransition(job.getState(), HEADJobState.EXPIRED)) {
            throw new HEADInvalidJobTransitionException(job.getId(), job.getState(), HEADJobState.EXPIRED, "expire()");
        }
        job.setCancelledAt(now);
        job.setCancelledBy(HEADCancelledBy.SYSTEM);
        job.setCancelReason(HEADCancelReason.REASSIGNED); // o EXPIRED si lo defines
        job.setState(HEADJobState.EXPIRED);
    }

    public void withdraw(HEADJob job, Instant now, String note) {
        if (!canTransition(job.getState(), HEADJobState.WITHDRAWN)) {
            throw new HEADInvalidJobTransitionException(job.getId(), job.getState(), HEADJobState.WITHDRAWN, "withdraw()");
        }
        job.setCancelledAt(now);
        job.setCancelledBy(HEADCancelledBy.SYSTEM);
        job.setCancelReason(HEADCancelReason.REASSIGNED);
        job.setCancelNote(trim255(note));
        job.setState(HEADJobState.WITHDRAWN);
    }

    public void cancelByClient(HEADJob job, Instant now, HEADCancelReason reason, String note) {
        transitToCancel(job, now, HEADCancelledBy.CLIENT, reason, note);
    }

    public void cancelByStaff(HEADJob job, Instant now, HEADCancelReason reason, String note) {
        transitToCancel(job, now, HEADCancelledBy.STAFF, reason, note);
    }

    public void cancelBySystem(HEADJob job, Instant now, HEADCancelReason reason, String note) {
        transitToCancel(job, now, HEADCancelledBy.SYSTEM, reason, note);
    }

    private void transitToCancel(HEADJob job, Instant now, HEADCancelledBy by, HEADCancelReason reason, String note) {
        if (!canTransition(job.getState(), HEADJobState.CANCELLED)) {
            throw new HEADInvalidJobTransitionException(job.getId(), job.getState(), HEADJobState.CANCELLED, "cancel()");
        }
        job.setCancelledAt(now);
        job.setCancelledBy(by);
        job.setCancelReason(reason);
        job.setCancelNote(trim255(note));
        job.setState(HEADJobState.CANCELLED);
    }

    private static String trim255(String s) {
        if (s == null) return null;
        return s.length() > 255 ? s.substring(0, 255) : s;
    }

    // Nuevo Helper en HEADJobStateMachine

    /**
     * Inicia el ciclo de oferta, asignando el trabajo a un conductor y estableciendo el tiempo.
     */
    public void offer(HEADJob job, HEADPersonalUser staff, Instant now) {
        // 1. Determinar el estado FROM:
        // Podría venir de un estado inicial (ej. PENDING) o de un estado de reasignación (ej. REJECTED/EXPIRED).
        // Tu mapa ALLOWED probablemente necesita una entrada para el estado inicial,
        // por ejemplo: ALLOWED.put(HEADJobState.PENDING_ASSIGNMENT, Set.of(HEADJobState.OFFERED));

        // Por ahora, asumimos que la transición es válida, pero es crucial validar el estado.
        HEADJobState from = job.getState();
        if (from != HEADJobState.PENDING_ASSIGNMENT &&
                from != HEADJobState.REJECTED &&
                from != HEADJobState.EXPIRED &&
                from != HEADJobState.WITHDRAWN)
        {
            // Lanza una excepción si no está en un estado válido para una nueva oferta
            throw new HEADBadRequestException(HEADJobState.OFFERED.name());
        }

        // 2. Aplicar los campos específicos de la oferta
        job.setStaffUser(staff);             // Asignar el nuevo conductor
        job.setStaffUuid(staff.getUidUser());   // Guardar el UUID para Fast Data
        job.setAssignedAt(now);              // La hora a la que se le ofrece al conductor actual
        // Nota: El 'offerExpiresAt' debe ser establecido en el HEADJobService,
        // ya que requiere lógica de negocio (now + 15 segundos).

        // 3. Establecer el nuevo estado
        job.setState(HEADJobState.OFFERED);
    }

    // Nuevo Helper en HEADJobStateMachine

    /**
     * Confirma la aceptación de la oferta por parte del conductor.
     */
    public HEADJob accept(HEADJob job, HEADPersonalUser staff, Instant now) {
        if (!canTransition(job.getState(), HEADJobState.ACCEPTED)) {
            throw new HEADInvalidJobTransitionException(job.getId(), job.getState(), HEADJobState.ACCEPTED, "accept()");
        }

        // Validación de seguridad: Asegurar que el conductor que acepta es el que recibió la oferta
        if (job.getStaffUser() == null || !job.getStaffUser().equals(staff)) {
            throw new HEADBadRequestException("The accepting staff member does not match the assigned staff member for this offer.");
        }

        // Actualizar el timestamp
        job.setAcceptedAt(now);

        // Establecer el nuevo estado
        job.setState(HEADJobState.ACCEPTED);

        return job;
    }
}

