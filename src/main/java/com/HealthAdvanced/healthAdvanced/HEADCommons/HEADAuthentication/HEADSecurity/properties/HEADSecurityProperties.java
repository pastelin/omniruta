package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.enums.HEADRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "head.security")
public class HEADSecurityProperties {

    @Data
    public static class Jwt {
        @NotBlank
        private String secret;
        @NotBlank private String issuer;
        @NotBlank private String audience;
        @Min(60)  private long ttlSeconds = 900;
        @Min(900) private long ttlSecondsAdmin = 7000;
        @Min(0)   private long clockSkewSeconds = 60;
    }

    @Data
    public static class Paths {
        private List<String> publicPaths = new ArrayList<>();
        private Map<String, List<String>> byRole = Map.of();
    }

    @Data
    public static class Headers {
        private List<String> required = new ArrayList<>();
        private List<String> optional = new ArrayList<>();
        private Map<String, String> aliases = Map.of();
        @JsonProperty("enforce-on-public")
        private boolean enforceOnPublic = false;
    }

    private Jwt jwt = new Jwt();
    private Paths paths = new Paths();
    private Headers headers = new Headers();


    // Helper para obtener rutas por rol como authorities de Spring
    public Map<String, List<String>> roleToPatterns() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        paths.getByRole().forEach((roleName, patterns) -> {
            // Validar role existente
            HEADRole.valueOf(roleName); // lanzará IAE si no existe
            map.put(roleName, patterns);
        });
        return map;
    }
}