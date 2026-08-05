package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.enums.HEADDocAction;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADDocRequirementItemDTO {
    private Integer documentId;
    private String name;
    private String description;
    private String typeFile;               // "image", "pdf", etc.
    private List<String> allowedExtensions;
    private Boolean required;
    private Boolean repeatAllowed;
    private Integer maxRepeats;

    // Estado del staff
    private String status;                 // NOT_UPLOADED | PENDING | APPROVED | REJECTED
    private Boolean uploaded;
    private HEADFileInfoDTO file;
    @JsonSerialize(using = ToStringSerializer.class)// null si no hay
    private java.time.LocalDateTime uploadedAt;
    @JsonSerialize(using = ToStringSerializer.class)
    private java.time.LocalDateTime reviewedAt;
    private String reviewNotes;

    // UI
    private HEADUiFlagsDTO ui;
    private List<HEADDocAction> actions;
    private Boolean requiresLicenseNo;
    private String licenseLabel;
}