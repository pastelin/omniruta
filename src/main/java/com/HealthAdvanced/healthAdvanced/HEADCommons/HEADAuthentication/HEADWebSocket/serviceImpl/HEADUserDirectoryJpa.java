package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.serviceImpl;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADUserDirectory;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class HEADUserDirectoryJpa implements HEADUserDirectory {

    private final HEADPersonalUserRepository repo;
    private final HEADClientsRepository clientsRepo;

    public HEADUserDirectoryJpa(HEADPersonalUserRepository repo,HEADClientsRepository clientsRepo) {
        this.repo = repo;
        this.clientsRepo = clientsRepo;
    }

    @Override
    public Long findStaffUserIdByUuid(String staffUuid) {
        if (staffUuid == null || staffUuid.isBlank()) return null;
        return repo.findIdByUuid(staffUuid).orElse(null);
    }

    @Override
    public Long findClientUserIdByUuid(String clientUuid) {
        if (clientUuid == null || clientUuid.isBlank()) return null;
        return clientsRepo.findIdByUuid(clientUuid).orElse(null);
    }
}
