package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.maps;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADGetUserProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.models.response.HEADAppointmentsResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HEADAppointmentMapper {

    public static HEADAppointmentsResponse.AppointmentItem toItem(HEADJob j) {

        String title = j.getRequest().getPkg().getTitle();
        String category = j.getRequest().getPkg().getSubtitle(); // "Psicología", "Laboratorio"...

        Instant when = firstNonNull(j.getScheduledTime(), j.getScheduledAt(), j.getCreatedAt());

        HEADServiceMode mode = j.getServiceMode() != null
                ? j.getServiceMode()
                : j.getRequest().getPkg().getServiceMode();

        String providerName = (j.getStaffUser() == null) ? null : buildStaffName(j.getStaffUser());

        String locationName = (mode == HEADServiceMode.VIDEO)
                ? "Videollamada"
                : firstNonBlank(j.getRequest().getStartAddress(), j.getStartAddress());

        HEADAppointmentsResponse.AppointmentStatus status = mapStatus(j.getState());

        return new HEADAppointmentsResponse.AppointmentItem(
                j.getId().toString(),
                title,
                status,
                providerName,
                category,
                when,
                mode,
                locationName
        );
    }

    private static Instant firstNonNull(Instant a, Instant b, Instant c) {
        if (a != null) return a;
        if (b != null) return b;
        return c;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private static String buildStaffName(HEADPersonalUser s) {
        return (s.getNombre() + " " + s.getAPaterno()).trim();
    }

    public static HEADAppointmentsResponse.AppointmentStatus mapStatus(HEADJobState state) {
        return switch (state) {
            case SCHEDULED, ACCEPTED_AWAITING_START, ACCEPTED -> HEADAppointmentsResponse.AppointmentStatus.CONFIRMED;

            case EN_ROUTE, ARRIVED, STARTED, PAUSED, READY -> HEADAppointmentsResponse.AppointmentStatus.IN_PROGRESS;

            case COMPLETED -> HEADAppointmentsResponse.AppointmentStatus.COMPLETED;

            case CANCELLED, REJECTED, EXPIRED, WITHDRAWN, UNASSIGNABLE -> HEADAppointmentsResponse.AppointmentStatus.CANCELLED;

            case OFFERED, SCHEDULE_PENDING, PENDING_ASSIGNMENT -> HEADAppointmentsResponse.AppointmentStatus.PENDING;
        };
    }
}