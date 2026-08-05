package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class HEADStaffStateNearbyWiring {
    private final HEADStaffStateStore staffState;
    private final HEADNearbyService nearby;

}
