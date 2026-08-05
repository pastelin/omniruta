package com.HealthAdvanced.healthAdvanced.ModelsBD.repositories;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IHEADSexUserRepository extends JpaRepository<HEADSexUser,Long> {
}
