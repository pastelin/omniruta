package com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HEADPrescriptionJpaRepository extends JpaRepository<HEADPrescription, Long> {
    boolean existsByJob_Id(Long jobId);
    Optional<HEADPrescription> findByJob_Id(Long jobId);

    Optional<HEADPrescription> findByJob_IdAndClientUuid(Long jobId, String clientUuid);

    @Query("""
        select p
        from HEADPrescription p
        left join fetch p.medications m
        where p.job.id = :jobId
          and p.clientUuid = :clientUuid
    """)
    Optional<HEADPrescription> findByJobIdAndClientUuidWithMeds(
            @Param("jobId") Long jobId,
            @Param("clientUuid") String clientUuid
    );

    long countByClientUuidAndStatusIn(
            String clientUuid,
            Collection<HEADPrescriptionStatus> statuses
    );

    @Query("""
    select count(p.id)
    from HEADPrescription p
    where p.clientUuid = :clientUuid
      and p.status = :status
      and p.issuedAt >= :from
""")
    long countActivePrescriptionsLastDays(
            @Param("clientUuid") String clientUuid,
            @Param("status") HEADPrescriptionStatus status,
            @Param("from") Instant from
    );

    @Query("""
    select coalesce(count(m.id), 0)
    from HEADPrescription p
    join p.medications m
    where p.clientUuid = :clientUuid
      and p.status = :status
      and p.issuedAt >= :from
""")
    long countActiveMedicationsLastDays(
            @Param("clientUuid") String clientUuid,
            @Param("status") HEADPrescriptionStatus status,
            @Param("from") Instant from
    );


    @EntityGraph(attributePaths = {"medications"})
    List<HEADPrescription> findByClientUuidOrderByIssuedAtDesc(String clientUuid);

}
