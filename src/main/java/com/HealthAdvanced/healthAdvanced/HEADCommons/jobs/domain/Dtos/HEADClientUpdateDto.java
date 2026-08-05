package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

/**
 * DTO que encapsula las actualizaciones de estado o ubicación enviadas al cliente.
 */
public record HEADClientUpdateDto(
        // Identificación
        Long jobId,

        // Estado y Tiempo
        String currentJobState, // El estado actual del Job (Ej: "EN_ROUTE", "ARRIVED", "ACCEPTED")
        String statusMessage,   // Mensaje legible para el cliente (Ej: "Conductor en camino", "Conductor ha llegado")

        // Ubicación del Conductor
        Double driverLat,
        Double driverLng,

        // Información del Conductor (para el Front-end)
        String driverName,
        String driverPhotoUrl,
        String vehicleModel,
        String vehiclePlate,

        // Tiempos estimados
        Integer estimatedTimeOfArrivalSeconds // ETA del conductor al punto de recogida
) {}


