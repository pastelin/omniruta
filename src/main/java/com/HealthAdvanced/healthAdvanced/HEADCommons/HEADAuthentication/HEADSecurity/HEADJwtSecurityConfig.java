package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADAudienceValidator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters.HEADJwtRequestFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters.HEADRequiredHeadersFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters.HEADWiretapAuthHeaderFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters.HEADCorrelationIdFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.handler.HEADAccessDeniedJsonHandler;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.handler.HEADAuthEntryPoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

import java.time.Duration;

import static org.springframework.security.config.Customizer.withDefaults;


@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(HEADSecurityProperties.class)
public class HEADJwtSecurityConfig {

    private final HEADSecurityProperties props;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   HEADCorrelationIdFilter headCorrelationIdFilter,
                                                   HEADJwtRequestFilter filter,
                                                   JwtDecoder decoder,
                                                   JwtAuthenticationConverter conv,
                                                   HEADRequiredHeadersFilter headRequiredHeadersFilter,
                                                   HEADWiretapAuthHeaderFilter headWiretapAuthHeaderFilter,
                                                   HEADAuthEntryPoint headAuthEntryPoint,
                                                   HEADAccessDeniedJsonHandler headAccessDeniedJsonHandler
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> {
                    if (!props.getPaths().getPublicPaths().isEmpty()) {
                        auth.requestMatchers(props.getPaths().getPublicPaths().toArray(String[]::new)).permitAll();
                    }

                    props.getPaths().getByRole().forEach((roleName, patterns) ->
                            auth.requestMatchers(patterns.toArray(String[]::new)).hasRole(roleName)
                    );

                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(headAuthEntryPoint)
                        .accessDeniedHandler(headAccessDeniedJsonHandler)
                )
                .addFilterBefore(headCorrelationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(headRequiredHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(headWiretapAuthHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth -> oauth.jwt(j -> j.decoder(decoder).jwtAuthenticationConverter(conv)));

        return http.build();
    }

    @Bean
    FilterRegistrationBean<HEADCorrelationIdFilter> disableCorrelationIdFilterRegistration(HEADCorrelationIdFilter filter) {
        FilterRegistrationBean<HEADCorrelationIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<HEADJwtRequestFilter> disableJwtFilterRegistration(HEADJwtRequestFilter filter) {
        FilterRegistrationBean<HEADJwtRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<HEADRequiredHeadersFilter> disableHeadersFilterRegistration(HEADRequiredHeadersFilter filter) {
        FilterRegistrationBean<HEADRequiredHeadersFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<HEADWiretapAuthHeaderFilter> disableWiretapFilterRegistration(HEADWiretapAuthHeaderFilter filter) {
        FilterRegistrationBean<HEADWiretapAuthHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @PostConstruct
    void logSecurityProps() {
        log.info("Public paths: {}", props.getPaths().getPublicPaths());
        log.info("By role: {}", props.getPaths().getByRole());
        log.info("JWT issuer: {}, audience: {}", props.getJwt().getIssuer(), props.getJwt().getAudience());
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(gac);
        return conv;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(props.getJwt().getSecret());
        SecretKey key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
        var dec = NimbusJwtDecoder.withSecretKey(key).build();

        var withIssuer = JwtValidators.createDefaultWithIssuer(props.getJwt().getIssuer());
        var withSkew   = new JwtTimestampValidator(Duration.ofSeconds(props.getJwt().getClockSkewSeconds()));
        var withAud    = new HEADAudienceValidator(props.getJwt().getAudience());
        dec.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAud, withSkew));
        return dec;
    }
}

