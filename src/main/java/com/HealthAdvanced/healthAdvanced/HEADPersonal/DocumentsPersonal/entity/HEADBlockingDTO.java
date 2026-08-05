package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.enums.HEADBlockingReasonCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADBlockingDTO {
    private String title;
    private String subtitle;
    private HEADBlockingReasonCode reasonCode; // enums para front
}