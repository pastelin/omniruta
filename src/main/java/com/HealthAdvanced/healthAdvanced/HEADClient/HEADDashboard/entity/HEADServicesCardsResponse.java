package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import lombok.Builder;

import java.util.List;

@Builder
public record HEADServicesCardsResponse(List<HEADServiceCardDto> items) {
}