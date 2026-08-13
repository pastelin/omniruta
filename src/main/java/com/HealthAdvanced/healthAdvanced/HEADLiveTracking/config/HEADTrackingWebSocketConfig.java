package com.HealthAdvanced.healthAdvanced.HEADLiveTracking.config;

import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.service.HEADTrackingLocationService;
import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.ws.HEADTrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registra el endpoint WebSocket nativo /ws/tracking usado por el demo de tracking en vivo.
 * Independiente del servidor Socket.IO (netty-socketio) usado por el resto de la app,
 * para poder probar el tracking sin depender del handshake JWT de HEADWebSocketConfig.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class HEADTrackingWebSocketConfig implements WebSocketConfigurer {

    private final HEADTrackingLocationService trackingLocationService;

    @Bean
    public HEADTrackingWebSocketHandler trackingWebSocketHandler() {
        return new HEADTrackingWebSocketHandler(trackingLocationService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(trackingWebSocketHandler(), "/ws/tracking")
                .setAllowedOriginPatterns("*"); // demo: abierto a cualquier origen, restringir antes de producción
    }
}
