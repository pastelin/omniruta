package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces;

public interface HEADUserDirectory {
    Long findStaffUserIdByUuid(String staffUuid);
    Long findClientUserIdByUuid(String clientUuid);
}
