package com.HealthAdvanced.healthAdvanced.ModelsBD.Enums;

public enum HEADDocumentStatus {
    NOT_UPLOADED,
    PENDING,     // subido, esperando revisión
    APPROVED,    // aprobado por admin
    REJECTED    // rechazado; requiere reenvío con motivo
}
