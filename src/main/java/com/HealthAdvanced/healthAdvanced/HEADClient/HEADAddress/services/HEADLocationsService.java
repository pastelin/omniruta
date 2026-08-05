package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.intefaces.HEADLocationAggView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.model.response.HEADLocationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADLocationsService {

    private final HEADJobRepository headJobRepository;
    private final HEADClientsRepository clientsRepository;
    private final HEADJwtGenerator headJwtGenerator;

    public HEADLocationsResponse getLocations() {
        String clientUuid = headJwtGenerator.getUserNamePersonalUser();

        HEADClients client = clientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new IllegalArgumentException("Client not found for uuid: " + clientUuid));

        long clientId = client.getIdUser();

        List<HEADLocationAggView> rows = headJobRepository.findLocationAggByClient(clientId);

        if (rows.isEmpty()) {
            return new HEADLocationsResponse(List.of());
        }

// ✅ más reciente (por lastUsedAt)
        String mostRecentAddr = rows.stream()
                .max(Comparator.comparing(HEADLocationAggView::getLastUsedAt))
                .map(HEADLocationAggView::getAddress)
                .orElse(null);

// ✅ más usada (por timesUsed; desempate por lastUsedAt)
        String mostUsedAddr = rows.stream()
                .max(Comparator
                        .comparingLong(HEADLocationAggView::getTimesUsed)
                        .thenComparing(HEADLocationAggView::getLastUsedAt))
                .map(HEADLocationAggView::getAddress)
                .orElse(null);

        List<HEADLocationsResponse.LocationItem> items = rows.stream()
                .map(r -> {
                    boolean isRecent = mostRecentAddr != null && mostRecentAddr.equals(r.getAddress());
                    boolean isPrimary = mostUsedAddr != null && mostUsedAddr.equals(r.getAddress()) && !isRecent;

                    return new HEADLocationsResponse.LocationItem(
                            stableId(r.getAddress()),
                            r.getAddress(),
                            isPrimary,
                            isRecent,
                            (int) r.getTimesUsed(),
                            r.getLastUsedAt()
                    );
                })
                .toList();

        return new HEADLocationsResponse(items);
    }

    private String stableId(String address) {
        // id estable para el front (key). Si prefieres SHA-256 lo hacemos luego.
        return Integer.toHexString(address.hashCode());
    }
}