package com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import lombok.Data;

@Data
public class SaveResult {
    private HEADActivePersonal headClient;
    private HEADActiveLocationPersonal selected; // opcional
}
