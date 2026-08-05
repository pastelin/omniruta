package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HEADStaffProfessionalProfileRepository extends JpaRepository<HEADStaffProfessionalProfile, Long> {

    Optional<HEADStaffProfessionalProfile> findByStaffUser_IdUser(Long staffUserId);
}