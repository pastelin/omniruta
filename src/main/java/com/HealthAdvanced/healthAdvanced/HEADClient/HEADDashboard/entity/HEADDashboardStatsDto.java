package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HEADDashboardStatsDto {
    private Integer nextAppointmentsCount;
    private String  nextAppointmentLabel;   // "Feb 22 • 14:00"

    private Integer activePrescriptionsCount;
    private String  prescriptionsLabel;     // "Activas"

    private Integer pointsTotal;
    private String  pointsDeltaLabel;
}
