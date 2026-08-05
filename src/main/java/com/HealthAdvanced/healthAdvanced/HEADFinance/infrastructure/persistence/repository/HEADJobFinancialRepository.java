package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;


import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADStaffEarningsAggregate;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions.HEADMyEarningsTransactionView;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions.HEADStaffEarningTransactionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HEADJobFinancialRepository extends JpaRepository<HEADJobFinancial, Long> {

    boolean existsByJobId(Long jobId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select f
        from HEADJobFinancial f
        where f.jobId = :jobId
    """)
    Optional<HEADJobFinancial> findByJobIdForUpdate(@Param("jobId") Long jobId);

    @Query("""
        select new com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADStaffEarningsAggregate(
            count(f.jobId),
            coalesce(sum(f.staffPayoutAmount), 0),
            coalesce(sum(coalesce(j.durationSeconds, 0)), 0)
        )
        from HEADJobFinancial f
        join f.job j
        where f.staffUser.idUser = :staffId
          and f.completedAt >= :from
          and f.completedAt < :to
    """)
    HEADStaffEarningsAggregate aggregateStaffEarnings(@Param("staffId") Long staffId,
                                                      @Param("from") Instant from,
                                                      @Param("to") Instant to);

    @Query(
            value = """
            select
                f.jobId as jobId,
                p.title as serviceName,
                c.nombre as nombre,
                c.aPaterno as paterno,
                f.staffPayoutAmount as amount,
                cast(f.payoutStatus as string) as payoutStatus,
                f.completedAt as completedAt
            from HEADJobFinancial f
            join f.job j
            join j.client c
            join j.request req
            join req.pkg p
            where f.staffUser.idUser = :staffId
              and f.completedAt >= :from
              and f.completedAt < :to
            order by f.completedAt desc
        """,
            countQuery = """
            select count(f.jobId)
            from HEADJobFinancial f
            where f.staffUser.idUser = :staffId
              and f.completedAt >= :from
              and f.completedAt < :to
        """
    )
    Page<HEADStaffEarningTransactionView> findStaffTransactions(@Param("staffId") Long staffId,
                                                                @Param("from") Instant from,
                                                                @Param("to") Instant to,
                                                                Pageable pageable);

    @Query("""
        select coalesce(sum(f.staffPayoutAmount), 0)
        from HEADJobFinancial f
        where f.staffUser.idUser = :staffId
          and f.currency = :currency
          and f.payoutStatus = :status
          and f.payoutAvailableAt <= :now
          and f.completedAt >= :from
          and f.completedAt < :to
    """)
    BigDecimal sumAvailableForPayout(@Param("staffId") Long staffId,
                                     @Param("currency") String currency,
                                     @Param("status") HEADJobPayoutStatus status,
                                     @Param("now") Instant now,
                                     @Param("from") Instant from,
                                     @Param("to") Instant to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select f
        from HEADJobFinancial f
        where f.staffUser.idUser = :staffId
          and f.currency = :currency
          and f.payoutStatus = :status
          and f.payoutAvailableAt <= :now
          and f.completedAt >= :from
          and f.completedAt < :to
        order by f.completedAt asc, f.jobId asc
    """)
    List<HEADJobFinancial> findEligibleForPayoutForUpdate(@Param("staffId") Long staffId,
                                                          @Param("currency") String currency,
                                                          @Param("status") HEADJobPayoutStatus status,
                                                          @Param("now") Instant now,
                                                          @Param("from") Instant from,
                                                          @Param("to") Instant to);

    @Modifying
    @Query("""
        update HEADJobFinancial f
        set f.payoutStatus = :toStatus
        where f.payoutStatus = :fromStatus
          and f.payoutAvailableAt <= :now
    """)
    int releasePayouts(@Param("fromStatus") HEADJobPayoutStatus fromStatus,
                       @Param("toStatus") HEADJobPayoutStatus toStatus,
                       @Param("now") Instant now);

    @Query("""
    select coalesce(sum(f.staffPayoutAmount), 0)
    from HEADJobFinancial f
    where f.staffUser.idUser = :staffId
      and f.completedAt >= :from
      and f.completedAt < :to
""")
    BigDecimal sumStaffPayoutByRange(@Param("staffId") Long staffId,
                                     @Param("from") Instant from,
                                     @Param("to") Instant to);

    @Query("""
    select
        f.jobId as id,
        p.title as type,
        c.nombre as nombre,
        c.aPaterno as paterno,
        f.staffPayoutAmount as amount,
        cast(f.payoutStatus as string) as payoutStatus,
        f.completedAt as completedAt
    from HEADJobFinancial f
    join f.job j
    join j.client c
    join j.request req
    join req.pkg p
    where f.staffUser.idUser = :staffId
      and f.completedAt >= :from
      and f.completedAt < :to
    order by f.completedAt desc
""")
    List<HEADMyEarningsTransactionView> findMyEarningsTransactions(
            @Param("staffId") Long staffId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}