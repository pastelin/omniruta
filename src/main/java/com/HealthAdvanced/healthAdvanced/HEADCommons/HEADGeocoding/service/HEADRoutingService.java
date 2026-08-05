package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.Dto.HEADRouteDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response.*;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service.HEADServiceGeneric;
import com.google.maps.internal.PolylineEncoding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADRoutingService {

    @Value("${google.maps.api.key:}")
    private String apiKey;

    private final HEADServiceGeneric webClient;

    public HEADRouteDto routeStaffToClient(double fromLat, double fromLng, double toLat, double toLng) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new HEADBadRequestException("Google API key missing");
        }

        var origin = fromLat + "," + fromLng;
        var dest   = toLat + "," + toLng;

        var wc = webClient.webClientBuilder();

        HEADGoogleDirectionsResponse resp = wc.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/api/directions/json")
                        .queryParam("origin", origin)
                        .queryParam("destination", dest)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(HEADGoogleDirectionsResponse.class)
                .block();

        if (resp == null || resp.routes() == null || resp.routes().isEmpty()) {
            throw new HEADBadRequestException("Directions sin rutas. status=" + (resp != null ? resp.status() : "null"));
        }

        HEADGoogleRoute route = resp.routes().get(0);
        if (route.legs() == null || route.legs().isEmpty()) {
            throw new HEADBadRequestException("Directions sin legs.");
        }

        HEADGoogleLeg leg = route.legs().get(0);

        var pts = PolylineEncoding.decode(route.overviewPolyline().points());

        var statsLat = pts.stream()
                .collect(java.util.stream.Collectors.summarizingDouble(p -> p.lat));
        var statsLng = pts.stream()
                .collect(java.util.stream.Collectors.summarizingDouble(p -> p.lng));

        double northLat = statsLat.getMax();
        double southLat = statsLat.getMin();
        double eastLng  = statsLng.getMax();
        double westLng  = statsLng.getMin();
        return new HEADRouteDto(
                leg.distance().value(),     // meters
                leg.duration().value(),     // seconds
                leg.startAddress(),
                leg.endAddress(),
                route.overviewPolyline().points(),
                northLat,
                eastLng,
                southLat,
                westLng

        );
    }

}
