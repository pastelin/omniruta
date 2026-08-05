package com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackageAvailableDto;
import lombok.Data;

// Lo que recibe EL PERSONAL cuando un cliente lo selecciona
@Data
public class PersonalAssignmentPayload {
    private String uuIdClient;          // para conversación/seguimiento
    private double clientLat;
    private double clientLng;
    private long distanceMts;           // distancia personal->cliente
    private int etaMin;                 // ETA en minutos
    private String note;                // opcional (p.ej. observaciones del request)
    private Long idActivePersonal;      // opcional (registro vínculo)
    private HEADPackageAvailableDto requestPackage;
    // ... agrega campos del cliente si quieres (nombre, dirección, etc.)
}

