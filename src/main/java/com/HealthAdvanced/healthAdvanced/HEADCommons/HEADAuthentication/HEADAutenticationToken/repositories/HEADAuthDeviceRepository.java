package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB.HEADAuthDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HEADAuthDeviceRepository extends JpaRepository<HEADAuthDevice, Long> {
    Optional<HEADAuthDevice> findByUserIdAndDeviceId(Long userId, String deviceId);
}