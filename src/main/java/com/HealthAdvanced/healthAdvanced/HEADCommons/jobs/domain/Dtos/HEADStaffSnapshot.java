package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;

public record HEADStaffSnapshot(HEADStaffStateDto state, boolean connected) {}
