package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HEADOccupationsRepository extends JpaRepository<HEADOccupations, Integer> {
    @Query("SELECT o FROM HEADOccupations o ORDER BY o.nameOccupation ASC")
    List<HEADOccupations> findCatalog();
}
