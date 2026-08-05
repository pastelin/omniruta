package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADMovePersonalToClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface HEADMovePersonalToClientRepository extends JpaRepository<HEADMovePersonalToClient, Long> {
    Optional<List<HEADMovePersonalToClient>> findByIdActiveLocationPersonal(HEADActiveLocationPersonal idActiveLocationPersonal);

    @Query("SELECT u FROM HEADMovePersonalToClient u " +
            "WHERE u.idServiceRequestClient.idServiceRequestClient = :idServiceClient " +
            "AND u.idActiveLocationPersonal.idActivePersonal = :idActivePersonal ORDER BY idStaffToClient LIMIT 1")
    Optional<HEADMovePersonalToClient> findByServiceClient(
            @Param("idServiceClient") Long idServiceClient,
            @Param("idActivePersonal") Long idActivePersonal);

    @Query("SELECT u FROM HEADMovePersonalToClient u " +
            "WHERE u.idServiceRequestClient.idServiceRequestClient NOT IN :idServiceClient " +
            "AND u.idActiveLocationPersonal.idActivePersonal = :idActivePersonal")
    Stream<HEADMovePersonalToClient> findByActivePersonal(
            @Param("idServiceClient") List<Long> idServiceClient,
            @Param("idActivePersonal") Long idActivePersonal);
}
