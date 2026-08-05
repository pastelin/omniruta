package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.AuthorizationResult;

@Slf4j
@org.springframework.context.annotation.Configuration
public class HEADWebSocketConfig {

    @Value("${socket-server.host}")
    private String host;

    @Value("${socket-server.port}")
    private Integer port;

    @Bean
    public ObjectMapper socketObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public SocketIOServer socketIOServer(HEADJwtGenerator jwt) {
        Configuration cfg = new Configuration();
        cfg.setHostname(host);
        cfg.setPort(port);
        cfg.setTransports(Transport.WEBSOCKET, Transport.POLLING);

        cfg.setPingInterval(25_000);
        cfg.setPingTimeout(30_000);

        cfg.setAuthorizationListener(handshake -> {
            String token = handshake.getSingleUrlParam("token");

            if (token == null || token.isBlank()) {
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            try {
                var claims = jwt.extractAllClaims(token);


                return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;

            } catch (Exception e) {
                log.error("WS auth failed: {} - {}", e.getClass().getName(), e.getMessage(), e);
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }
        });

        // ✅ aquí ya no truena Instant
        cfg.setJsonSupport(new HEADJacksonJsonSupport());

        return new SocketIOServer(cfg);
    }
}
