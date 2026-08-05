package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response.HEADAddressComponent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response.HEADGoogleGeocodeResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response.HEADGoogleResult;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service.HEADServiceGeneric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADGeocodingService {

    @Value("${google.maps.api.key:}")
    private String apiKey;

    private final HEADServiceGeneric webClient;

    public String getAddressDescription(double lat, double lng) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GEO] apiKey vacía, salto geocoding");
            return null;
        }

        String latlng = lat + "," + lng;
        var serviceApiGeo = webClient.webClientBuilder();

        return serviceApiGeo.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/api/geocode/json")
                        .queryParam("latlng", latlng)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> {
                                    log.warn("[GEO] HTTP {} body={}", resp.statusCode(), body);
                                    return new RuntimeException("Geocode error " + resp.statusCode());
                                })
                )
                .bodyToMono(HEADGoogleGeocodeResponse.class)
                .map(this::extractAddressFromObject)
                .timeout(java.time.Duration.ofSeconds(2))
                .onErrorResume(ex -> {
                    log.warn("[GEO] falló geocode latlng={} err={}", latlng, ex.toString());
                    return reactor.core.publisher.Mono.justOrEmpty((String) null);
                })
                .block();
    }

    public String extractAddressFromObject(HEADGoogleGeocodeResponse resp) {
        if (resp == null || resp.results() == null || resp.results().isEmpty()) return null;

        List<HEADGoogleResult> results = resp.results();

        // 1) street_address con formatted_address no null
        var street = results.stream()
                .filter(r -> r.types() != null && r.types().contains("street_address"))
                .map(HEADGoogleResult::formatted_address)
                .filter(Objects::nonNull)
                .findFirst();

        if (street.isPresent()) return street.get();

        // 2) route + street_number
        var routePlusNumber = results.stream()
                .filter(r -> hasComponentType(r, "route") && hasComponentType(r, "street_number"))
                .findFirst()
                .flatMap(r -> buildStreetLine(r).or(() -> Optional.ofNullable(r.formatted_address())));

        if (routePlusNumber.isPresent()) return routePlusNumber.get();

        // 3) route solamente
        var routeOnly = results.stream()
                .filter(r -> hasComponentType(r, "route"))
                .findFirst()
                .flatMap(r -> buildStreetLine(r).or(() -> Optional.ofNullable(r.formatted_address())));

        if (routeOnly.isPresent()) return routeOnly.get();

        // 4) fallback seguro
        return results.stream()
                .map(HEADGoogleResult::formatted_address)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean hasComponentType(HEADGoogleResult r, String wanted) {
        return Optional.ofNullable(r.address_components())
                .stream()
                .flatMap(List::stream)
                .anyMatch(c -> c.types() != null && c.types().contains(wanted));
    }

    private Optional<String> buildStreetLine(HEADGoogleResult r) {
        var comps = Optional.ofNullable(r.address_components()).orElse(List.of());

        String route = comps.stream()
                .filter(c -> c.types() != null && c.types().contains("route"))
                .map(HEADAddressComponent::long_name)
                .findFirst()
                .orElse(null);

        if (route == null) return Optional.empty();

        String number = comps.stream()
                .filter(c -> c.types() != null && c.types().contains("street_number"))
                .map(HEADAddressComponent::long_name)
                .findFirst()
                .orElse(null);

        return Optional.of(number != null ? route + " " + number : route);
    }
}
