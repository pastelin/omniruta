package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections.HEADRatingCountProjection;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections.HEADRecentReviewProjection;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model.HEADStaffReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HEADStaffReviewRepository extends JpaRepository<HEADStaffReview, Long> {

    // Para asegurar 1 review por job
    boolean existsByJob_Id(Long jobId);

    // Para verificar que el job pertenece a ese cliente (si decides validar así)
    boolean existsByJob_IdAndIdUserClient_IdUser(Long jobId, Long clientId);

    // Para bayesiano
    @Query("select coalesce(avg(r.rating), 0) from HEADStaffReview r")
    double platformAvgRating();

    @Query("""
        select r.rating as rating, count(r.id) as cnt
        from HEADStaffReview r
        where r.idPersonalUser.idUser = :staffId
        group by r.rating
    """)
    List<HEADRatingCountProjection> countByRatingForStaff(@Param("staffId") long staffId);

    @Query("""
    select coalesce(avg(r.rating), 0)
    from HEADStaffReview r
    where r.idPersonalUser.idUser = :staffId
""")
    double avgRatingForStaff(@Param("staffId") long staffId);

    @Query("""
    select coalesce(avg(r.rating), 0)
    from HEADStaffReview r
    where r.idUserClient.idUser = :clientId
""")
    double satisfactionAvgForClient(@Param("clientId") long clientId);


    @Query("""
      select coalesce(avg(r.rating), 0)
      from HEADStaffReview r
      where r.idUserClient.idUser = :clientId
    """)
    double avgRatingForClientId(@Param("clientId") long clientId);

    @Query("""
        select coalesce(avg(r.rating), 0)
        from HEADStaffReview r
        where r.idUserClient.uuIdUser = :clientUuid
          and r.createdAt >= :from
    """)
    double avgRatingLastDays(@Param("clientUuid") String clientUuid,
                             @Param("from") Instant from);

    @Query("""
  select avg(r.rating)
  from HEADStaffReview r
  where r.idUserClient.idUser = :clientId
    and r.createdAt >= :from
""")
    Double avgRatingForClientIdSince(@Param("clientId") Long clientId, @Param("from") Instant from);

    @Query("""
    select avg(r.rating)
    from HEADStaffReview r
    where r.idPersonalUser.idUser = :staffId
      and r.createdAt >= :from
      and r.createdAt < :to
""")
    Double avgRatingForStaffBetween(@Param("staffId") Long staffId,
                                    @Param("from") Instant from,
                                    @Param("to") Instant to);

    @Query(
            value = """
        select
            r.id as id,
            c.nombre as nombre,
            c.aPaterno as paterno,
            r.rating as rating,
            coalesce(r.comment, '') as comment,
            r.createdAt as createdAt,
            coalesce(p.title, '') as serviceName
        from HEADStaffReview r
        join r.idUserClient c
        join r.job j
        join j.request req
        join req.pkg p
        where r.idPersonalUser.idUser = :staffId
        order by r.createdAt desc
    """,
            countQuery = """
        select count(r.id)
        from HEADStaffReview r
        where r.idPersonalUser.idUser = :staffId
    """
    )
    Page<HEADRecentReviewProjection> findRecentReviewsByStaffId(
            @Param("staffId") Long staffId,
            Pageable pageable
    );



}


