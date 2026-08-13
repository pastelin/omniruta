package com.HealthAdvanced.healthAdvanced.HEADLiveTracking.controller;

import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.dto.HEADVehicleLocationDto;
import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.service.HEADTrackingLocationService;
import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.ws.HEADTrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST mínima del demo de tracking en vivo (HEADLiveTracking).
 * Sin autenticación por ahora: rutas públicas (ver public-paths en application.yml).
 * No usar en producción tal cual; ver ANALISIS_PROYECTO.md para el plan de aseguramiento.
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class HEADTrackingController {

    private final HEADTrackingLocationService trackingLocationService;
    private final HEADTrackingWebSocketHandler trackingWebSocketHandler;

    @PostMapping("/vehicles/{vehicleId}/location")
    public ResponseEntity<HEADVehicleLocationDto> updateLocation(
            @PathVariable String vehicleId,
            @RequestBody HEADVehicleLocationDto body
    ) {
        body.setVehicleId(vehicleId);
        var saved = trackingLocationService.save(body);
        trackingWebSocketHandler.broadcastLocation(saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/vehicles/{vehicleId}/location")
    public ResponseEntity<HEADVehicleLocationDto> getLocation(@PathVariable String vehicleId) {
        var dto = trackingLocationService.get(vehicleId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<HEADVehicleLocationDto>> listActiveVehicles() {
        return ResponseEntity.ok(trackingLocationService.listActive());
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of("ok", true, "module", "HEADLiveTracking"));
    }
}
