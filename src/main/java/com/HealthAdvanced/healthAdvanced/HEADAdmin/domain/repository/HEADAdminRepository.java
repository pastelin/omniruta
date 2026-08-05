package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.repository;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.entity.HEADAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HEADAdminRepository extends JpaRepository<HEADAdmin, Long> {

    Optional<HEADAdmin> findByEmailIgnoreCase(String email);

    Optional<HEADAdmin> findByEmailIgnoreCaseAndActiveTrue(String email);

    boolean existsByEmailIgnoreCase(String email);
    Optional<HEADAdmin> findByUidAdmin(String uidAdmin);
    boolean existsByUidAdmin(String uidAdmin);
}