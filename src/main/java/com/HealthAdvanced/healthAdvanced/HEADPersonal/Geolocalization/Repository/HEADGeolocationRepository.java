package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Repository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface HEADGeolocationRepository extends JpaRepository<HEADActiveLocationPersonal, Long> {
    Optional<HEADActiveLocationPersonal> findByIdPersonalUser(HEADPersonalUser idPersonalUser);
    Optional<HEADActiveLocationPersonal> findByUuIdPersonal(String uuIdPersonal);


    @Query(value = "SELECT * FROM fast_home_health.active_location_personal u WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(u.latitude)))) < :radio " +
            "AND u.is_active_work = 1", nativeQuery = true)
    Stream<HEADActiveLocationPersonal> findShowStaffs(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radio") double radio);

    @Query("""
    SELECT alp FROM HEADActiveLocationPersonal alp
    WHERE alp.isActiveWork = true
      AND (alp.isBusy = false OR alp.isBusy IS NULL)
      AND alp.latitude  BETWEEN :latMin AND :latMax
      AND alp.longitude BETWEEN :lngMin AND :lngMax
  """)
    List<HEADActiveLocationPersonal> findActiveInBBox(
            @Param("latMin") double latMin,
            @Param("latMax") double latMax,
            @Param("lngMin") double lngMin,
            @Param("lngMax") double lngMax
    );

    @Query("""
  SELECT alp FROM HEADActiveLocationPersonal alp
  JOIN FETCH alp.idPersonalUser pu
  LEFT JOIN FETCH pu.occupationLinks links
  LEFT JOIN FETCH links.idOccupationProfile prof
  LEFT JOIN FETCH prof.idOccupation occ
  WHERE alp.isActiveWork = true
    AND (alp.isBusy = false OR alp.isBusy IS NULL)
    AND alp.latitude  BETWEEN :latMin AND :latMax
    AND alp.longitude BETWEEN :lngMin AND :lngMax
""")
    List<HEADActiveLocationPersonal> findActiveInBBoxWithOcc(
            @Param("latMin") double latMin, @Param("latMax") double latMax,
            @Param("lngMin") double lngMin, @Param("lngMax") double lngMax
    );
}
