package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.HEADStaffCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HEADStaffCertificationRepository extends JpaRepository<HEADStaffCertification, Long> {

    List<HEADStaffCertification> findByStaffUser_IdUserAndActiveTrueOrderBySortOrderAscIdAsc(Long staffUserId);

    Optional<HEADStaffCertification> findByIdAndStaffUser_IdUser(Long id, Long staffUserId);

    @Query("""
    select max(c.sortOrder)
    from HEADStaffCertification c
    where c.staffUser.idUser = :staffId
      and c.active = true
""")
    Optional<Integer> findMaxSortOrderByStaffUserId(@Param("staffId") Long staffId);
}