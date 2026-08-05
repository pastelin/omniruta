package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface HEADClientsRepository extends JpaRepository<HEADClients, Long> {
    Optional<HEADClients> findByUuIdUser(String uuIdUser);
    Optional<HEADClients> findByEmail(String email);
    Optional<HEADClients> findByTelefono(String telefono);
    Optional<HEADClients> findByGoogleSub(String googleSub);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
UPDATE clients_users
SET roles = TRIM(BOTH ',' FROM CONCAT(
        TRIM(BOTH ',' FROM REPLACE(CONCAT(',', COALESCE(roles, ''), ','), ',REGISTER_CLIENT,', ',')),
        CASE
            WHEN FIND_IN_SET('ACCESS_CLIENT', REPLACE(COALESCE(roles, ''), ' ', '')) > 0 THEN ''
            WHEN COALESCE(TRIM(roles), '') = '' THEN 'ACCESS_CLIENT'
            ELSE ',ACCESS_CLIENT'
        END
    )),
    is_accepted = TRUE
WHERE id_user = :idUser
""", nativeQuery = true)
    int promoteClientById(@Param("idUser") Long idUser);


    @Query("select u.idUser from HEADClients u where u.uuIdUser = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") String uuid);
}
