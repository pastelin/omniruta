package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientEmergencyContactRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository.HEADClientMedicalInfoRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories.HEADAuthRefreshTokenRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADClientStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.implementations.HEADPresenceRedisStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HEADDeleteClientAccountService {

    private final HEADClientsRepository clientsRepository;
    private final HEADClientMedicalInfoRepository clientMedicalInfoRepository;
    private final HEADClientEmergencyContactRepository clientEmergencyContactRepository;
    private final HEADAuthRefreshTokenRepository refreshTokenRepository;
    private final HEADPresenceStore presenceStore;
    private final HEADClientStateStore clientStateStore;
    private final PasswordEncoder passwordEncoder;
    private final HEADJwtGenerator jwtGenerator;

    @Transactional
    public void deleteClientAccount() {
        var clientUuid = jwtGenerator.getUserNamePersonalUser();
        HEADClients client = clientsRepository.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clientUuid));

        Long clientId = client.getIdUser();
        String oldUuid = client.getUuIdUser();

        clientMedicalInfoRepository.deleteByClient_IdUser(clientId);
        clientEmergencyContactRepository.deleteByClient_IdUser(clientId);

        refreshTokenRepository.revokeAllByUserId(clientId);

        for (String sessionId : presenceStore.sessionIdsFor(oldUuid)) {
            presenceStore.remove(sessionId);
        }

        clientStateStore.clear(oldUuid);

        anonymizeClient(client);

        clientsRepository.save(client);
    }

    private void anonymizeClient(HEADClients client) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        client.setNombre("DELETED");
        client.setAPaterno("CLIENT");
        client.setAMaterno(suffix);
        client.setFechaNacimiento(null);
        client.setTelefono(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        client.setEmail("deleted+client+" + suffix.toLowerCase() + "@docarya.invalid");
        client.setPassword(passwordEncoder.encode("DELETED-" + UUID.randomUUID() + "-" + System.nanoTime()));
        client.setIdSexUser(null);
        client.setGoogleSub(null);
        client.setAuthProvider(HEADAuthProvider.LOCAL);
        client.setRoles("DELETED_ACCOUNT");
        client.setIsAccepted(Boolean.FALSE);

        // client.setUuIdUser("deleted-" + UUID.randomUUID());
    }
}