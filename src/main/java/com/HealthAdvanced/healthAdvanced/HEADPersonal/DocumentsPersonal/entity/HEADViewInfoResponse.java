package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ✔️ Respuesta para "quiero ver este archivo"
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADViewInfoResponse {
    private boolean success;            // true/false
    private String kind;                // "PDF" | "IMAGE" | "OTHER"
    private String mimeType;            // application/pdf, image/jpeg, ...
    private Long fileId;                // id interno (opcional)
    private String streamUrl;           // tu endpoint seguro para ver inline (recomendado)
    private String htmlUrl;             // opcional: página con PDF.js si decides usar WebView
    private String message;             // opcional: errores o hints
}
