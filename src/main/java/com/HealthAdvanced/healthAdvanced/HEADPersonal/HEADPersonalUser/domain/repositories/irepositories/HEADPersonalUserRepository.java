package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADPersonalUserRepository extends JpaRepository<HEADPersonalUser, Long> {
    Optional<HEADPersonalUser> findBytelefono(String phoneNumber);
    Optional<HEADPersonalUser> findByEmail(String email);
    Optional<HEADPersonalUser> findByUidUser(String uidUser);

    @Modifying
    @Transactional
    @Query("""
UPDATE HEADPersonalUser p
   SET p.roles =
       CONCAT(
           CASE WHEN p.roles LIKE '%REGISTER_PERSONAL%'
                THEN REPLACE(p.roles, 'REGISTER_PERSONAL', '')
                ELSE p.roles
           END,
           CASE WHEN p.roles LIKE '%ACCESS_PERSONAL%'
                THEN '' ELSE ',ACCESS_PERSONAL' END
       )
 WHERE p.idUser = :idUser
""")
    int promotePersonalById(@Param("idUser") Long idUser);

    @Query("select u.idUser from HEADPersonalUser u where u.uidUser = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") String uuid);


    @Query("""
    select min(link.idOccupationProfile.IdOccupationProfile)
    from HEHOOccupationPersonalUser link
    where link.idPersonalUser.idUser = :staffId
""")
    Optional<Long> findPrimaryOccupationProfileId(@Param("staffId") Long staffId);

    @Query("""
        select distinct p
        from HEADPersonalUser p
        left join fetch p.occupationLinks ol
        left join fetch ol.idOccupationProfile op
        where p.isEnabled = false
           or p.isEnabled is null
           or p.roles = :registerRole
        order by p.idUser desc
    """)
    List<HEADPersonalUser> findStaffPendingReview(@Param("registerRole") String registerRole);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update HEADPersonalUser p
           set p.isEnabled = :isEnabled,
               p.roles = :role
         where p.idUser = :userId
    """)
    int updateAccessStatus(@Param("userId") Long userId,
                           @Param("isEnabled") Boolean isEnabled,
                           @Param("role") String role);

    Optional<HEADPersonalUser> findByGoogleSub(String googleSub);
}
