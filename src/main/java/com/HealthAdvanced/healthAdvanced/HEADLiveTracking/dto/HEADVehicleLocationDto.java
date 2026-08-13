package com.HealthAdvanced.healthAdvanced.HEADLiveTracking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Punto de ubicación de un vehículo/staff en vivo.
 * DTO de entrada (REST/WS) y de salida (broadcast) del módulo HEADLiveTracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HEADVehicleLocationDto {

    private String vehicleId;
    private Double lat;
    private Double lng;
    private Double headingDeg;   // rumbo, 0-360, opcional
    private Double speedKmh;     // opcional
    private Instant recordedAt;  // la asigna el servidor si viene nula
}
