package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class HEADPasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Si quieres soportar varios algoritmos:
        // return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new BCryptPasswordEncoder();
    }
}
