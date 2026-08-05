package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.enums.HEADAdminRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class HEADAdminResponse {
    private Long id;
    private String uidAdmin;
    private String fullName;
    private String email;
    private HEADAdminRole role;
    private Boolean active;
    private Instant lastLoginAt;
    private Instant createdAt;
}