package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto;

public record HEADStaffStateDto(boolean online, boolean busy, Integer countRejected, boolean hasServiceRequest,
                                Double lat, Double lng,
                                boolean isAppActive,
                                Long currentJobId, long updatedAt) {
}
