package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface HEADOccupationPersonalUserRepository extends JpaRepository<HEHOOccupationPersonalUser, Integer> {
    Optional<List<HEHOOccupationPersonalUser>> findByIdPersonalUser(HEADPersonalUser idPersonalUser);

    @Query("""
       select distinct pu.uidUser
       from HEHOOccupationPersonalUser eup
       join eup.idPersonalUser pu
       where eup.idOccupationProfile.IdOccupationProfile in :profileIds
    """)
    Set<String> findStaffUidsByProfileIds(@Param("profileIds") Set<Long> profileIds);


    @Query("""
        select concat(o.nameOccupation, ' ', p.nameTypeProfile)
        from HEHOOccupationPersonalUser eup
        join eup.idOccupationProfile p
        join p.idOccupation o
        where eup.idPersonalUser.idUser = :staffUserId
        order by eup.idOccupationPersonal asc
    """)
    List<String> findOccupationLabelsByStaffUserId(@Param("staffUserId") Long staffUserId);

    @Query("""
        select o.code
        from HEHOOccupationPersonalUser eup
        join eup.idOccupationProfile p
        join p.idOccupation o
        where eup.idPersonalUser.idUser = :staffUserId
        order by eup.idOccupationPersonal asc
    """)
    List<HEADOccupationCode> findOccupationCodesOrdered(@Param("staffUserId") Long staffUserId);

    default HEADOccupationCode findPrimaryOccupationCodeOrNull(Long staffUserId) {
        var list = findOccupationCodesOrdered(staffUserId);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Query("""
        select p.nameTypeProfile
        from HEHOOccupationPersonalUser eup
        join eup.idOccupationProfile p
        join p.idOccupation o
        where eup.idPersonalUser.idUser = :staffUserId
        order by eup.idOccupationPersonal asc
    """)
    List<String> findOccupationLabelByStaffUserId(@Param("staffUserId") Long staffUserId);

    void deleteByIdPersonalUser(HEADPersonalUser idPersonalUser);

    @Query("""
        select count(eup) > 0
        from HEHOOccupationPersonalUser eup
        where eup.idPersonalUser.idUser = :staffUserId
          and eup.idOccupationProfile.IdOccupationProfile = :occupationProfileId
    """)
    boolean existsStaffProfile(
            @Param("staffUserId") Long staffUserId,
            @Param("occupationProfileId") Long occupationProfileId);
}
