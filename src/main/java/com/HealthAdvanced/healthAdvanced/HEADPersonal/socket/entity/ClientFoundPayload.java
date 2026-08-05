package com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity;

import lombok.Data;

// Lo que recibe EL CLIENTE cuando ya hay un personal asignado/visible
@Data
public class ClientFoundPayload {
    private String uuIdPersonal;        // identificador del personal
    private Double personalLat;
    private Double personalLng;
    private long distanceMts;
    private int etaMin;
    private boolean accepted;           // true si ya fue “aceptado”, false si sólo está visible
    private String displayName;         // opcional del personal
    private String occupationName;      // opcional
    private String plateOrId;           // opcional para identificación
}