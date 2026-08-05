package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADErrorCommonsSocket {
    private Integer code;
    private String message;
    private Boolean isSuccess;
}
