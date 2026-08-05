package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.config;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADGoogleOauthProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADTurnProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.proposValues.values.HEADValuesProperties;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADStripeProperties;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.propertiesModel.TelnyxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({HEADSecurityProperties.class, HEADValuesProperties.class, HEADStripeProperties.class, HEADGoogleOauthProperties.class, HEADTurnProperties.class, TelnyxProperties.class})
public class HEADPropsConfig {}
