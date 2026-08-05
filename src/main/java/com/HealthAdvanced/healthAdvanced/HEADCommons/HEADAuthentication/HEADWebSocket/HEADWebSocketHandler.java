package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.HEADServiceAuthentications;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADPersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Objects;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity.*;

@Service
@RequiredArgsConstructor
public class HEADWebSocketHandler {

    @Autowired
    HEADJwtGenerator headJwtGenerator;
    @Autowired
    HEADPersonalService userHeadPersonalService;
    @Autowired
    private HEADServiceAuthentications headServiceAuthentications;

}
