package com.HealthAdvanced.healthAdvanced;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.configs.HEADFcmProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = {HEADSecurityProperties.class,  HEADFcmProperties.class, })
public class HealthAdvancedApplication {

	public static void main(String[] args) {
  		ConfigurableApplicationContext context = SpringApplication.run(HealthAdvancedApplication.class, args);
		SocketIOServer socketIOServer = context.getBean(SocketIOServer.class);
		socketIOServer.start();
		HEADCommonsUtils.passwordEncoder = new BCryptPasswordEncoder();
	}

}
