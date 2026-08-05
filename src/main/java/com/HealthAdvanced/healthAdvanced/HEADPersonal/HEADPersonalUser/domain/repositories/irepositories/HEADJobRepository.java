package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.intefaces.HEADLocationAggView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity.PackageCountView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.interfaces.HEADServiceHistoryRowViewGeneric;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.interfaces.HEADServiceTypeChipView;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces.HEADJobRefillView;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelledBy;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models.JobUuidsView;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.persistence.HEADCompletedServiceRowView;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.persistence.HEADServiceHistorySummaryView;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.persistence.HEADScheduledServiceRowView;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HEADJobRepository extends JpaRepository<HEADJob, Long> {
    //List<HEADJob> findTop10ByStaffUser_idUserOrderByCreatedAtDesc(Long staffId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from HEADJob j where j.id = :id")
    Optional<HEADJob> findByIdForUpdate(@Param("id") Long id);
    // IDs por staffUuid + estados
    @Query("""
        select j.id
          from HEADJob j
         where j.staffUuid = :staffUuid
           and j.state in :states
        """)
    List<Long> findIdsByStaffUuidAndStates(@Param("staffUuid") String staffUuid,
                                           @Param("states") Set<HEADJobState> states);

    // (Alternativa) IDs por staffUser.id + estados
    @Query("""
        select j.id
          from HEADJob j
         where j.staffUser.idUser = :staffUserId
           and j.state in :states
        """)
    List<Long> findIdsByStaffUserIdAndStates(@Param("staffUserId") Long staffUserId,
                                             @Param("states") Set<HEADJobState> states);

    @Query("""
        select j.id
          from HEADJob j
         where j.client.idUser = :staffUserId
           and j.state in :states
        """)
    List<Long> findIdsByClientUserIdAndStates(@Param("staffUserId") Long staffUserId,
                                             @Param("states") Set<HEADJobState> states);

    // (Si usas expiración de ofertas)
    List<HEADJob> findTop100ByStateAndOfferExpiresAtBefore(HEADJobState state, Instant now);

    List<HEADJob> findTop100ByStateAndCancelledAtBefore(
            HEADJobState state,
            Instant cancelledAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update HEADJob j
           set j.state = :accepted,
               j.acceptedAt = :now,
               j.updatedAt = :now,
               j.version = j.version + 1
         where j.id = :jobId
           and j.state = :offered
           and j.offerExpiresAt > :now
           and j.staffUuid = :staffUuid
    """)
    int acceptIfStillOffered(
            @Param("jobId") Long jobId,
            @Param("staffUuid") String staffUuid,
            @Param("now") Instant now,
            @Param("offered") HEADJobState offered,
            @Param("accepted") HEADJobState accepted
    );



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update HEADJob j
           set j.state = :expired,
               j.cancelledAt = :now,
               j.cancelledBy = :by,
               j.cancelReason = :reason,
               j.updatedAt = :now,
               j.version = j.version + 1
         where j.id = :jobId
           and j.state = :offered
           and j.offerExpiresAt <= :now
    """)
    int expireIfStillOffered(
            @Param("jobId") Long jobId,
            @Param("now") Instant now,
            @Param("offered") HEADJobState offered,
            @Param("expired") HEADJobState expired,
            @Param("by") HEADCancelledBy by,
            @Param("reason") HEADCancelReason reason
    );

    Optional<HEADJob> findFirstByStaffUuidAndStateInOrderByUpdatedAtDesc(
            String staffUuid,
            Collection<HEADJobState> states
    );

    @Query("""
       select j from HEADJob j
       where j.staffUser.idUser = :staffId
         and j.state in :states
         and FUNCTION('DATE', j.createdAt) = CURRENT_DATE
       order by j.createdAt desc
   """)
    List<HEADJob> findLatestActiveForStaff(
            @Param("staffId") Long staffId,
            @Param("states") Set<HEADJobState> states
    );

    @Query("""
    select j from HEADJob j
    where j.client.idUser = :clientId
      and j.state in :states
      and FUNCTION('DATE', j.createdAt) = CURRENT_DATE
    order by j.createdAt desc
""")
    List<HEADJob> findLatestActiveForClient(
            @Param("clientId") Long clientId,
            @Param("states") Set<HEADJobState> states
    );

    @Query("""
        select j.id as id,
               j.clientLat as clientLat,
               j.clientLng as clientLng,
               p.id as pkgId,
               j.serviceMode as serviceMode
        from HEADJob j
        left join j.request r
        left join r.pkg p
        where j.id = :jobId
    """)
    HEADJobRefillView findRefillView(@Param("jobId") Long jobId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update HEADJob j
           set j.state = :accepted,
               j.acceptedAt = :now,
               j.updatedAt = :now,
               j.version = j.version + 1
         where j.id = :jobId
           and j.state = :offered
           and j.offerExpiresAt > :now
           and j.staffUser.idUser = :staffUserId
    """)
    int acceptIfStillOfferedByStaffId(
            @Param("jobId") Long jobId,
            @Param("staffUserId") Long staffUserId,
            @Param("now") Instant now,
            @Param("offered") HEADJobState offered,
            @Param("accepted") HEADJobState accepted
    );

    @Query("""
        select j.client.uuIdUser
        from HEADJob j
        where j.id = :jobId
    """)
    String findClientUuidByJobId(@Param("jobId") Long jobId);

    @Query("""
        select j.staffUuid
        from HEADJob j
        where j.id = :jobId
    """)
    String findStaffUuidByJobId(@Param("jobId") Long jobId);

    @Query("""
        select j.client.uuIdUser as clientUuid, j.staffUuid as staffUuid, j.startedAt as startedAt
        from HEADJob j
        where j.id = :jobId
    """)
    Optional<JobUuidsView> findJobUuids(@Param("jobId") Long jobId);

    List<HEADJob> findAllByStateAndScheduledTimeBefore(HEADJobState state, Instant scheduledTime);

    List<HEADJob> findTop200ByStateAndScheduledTimeLessThanEqualOrderByScheduledTimeAsc(
            HEADJobState state, Instant time);


    List<HEADJob> findTop200ByStateAndScheduledTimeBetweenAndScheduleReminderSentFalseOrderByScheduledTimeAsc(
            HEADJobState state, Instant from, Instant to
    );

    @Query("""
           select j
           from HEADJob j
           where j.staffUuid = :staffUuid
             and j.scheduledTime is not null
             and j.scheduledTime >= :dayStart
             and j.scheduledTime <  :dayEnd
             and j.state in :states
           """)
    List<HEADJob> findScheduledJobsForDay(String staffUuid,
                                          Instant dayStart,
                                          Instant dayEnd,
                                          Collection<HEADJobState> states);

    @Query("""
   select j from HEADJob j
   left join fetch j.request r
   left join fetch r.pkg p
   where j.id = :id
""")
    Optional<HEADJob> findByIdWithRequestAndPkg(@Param("id") Long id);

    @Query("""
   select j from HEADJob j
   left join fetch j.request r
   left join fetch r.pkg p
   where j.staffUuid = :staffUuid
     and j.scheduledTime is not null
     and j.scheduledTime >= :from
     and j.scheduledTime <  :to
     and j.state in :states
""")
    List<HEADJob> findScheduledJobsBetweenWithPkg(
            @Param("staffUuid") String staffUuid,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("states") Collection<HEADJobState> states
    );

    @Modifying
    @Query("""
 update HEADJob j
    set j.state = :next, j.updatedAt = :now, j.version = j.version + 1
  where j.id = :jobId
    and j.state = :expected
""")
    int advanceState(@Param("jobId") Long jobId,
                     @Param("expected") HEADJobState expected,
                     @Param("next") HEADJobState next,
                     @Param("now") Instant now);

    // Próxima cita (la más cercana a futuro)
    Optional<HEADJob> findFirstByClient_IdUserAndStateAndScheduledAtAfterOrderByScheduledAtAsc(
            Long clientId,
            HEADJobState state,
            Instant now
    );

    long countByClient_IdUserAndStateAndScheduledTimeAfter(
            Long clientId,
            HEADJobState state,
            Instant now
    );

    Optional<HEADJob> findFirstByClient_IdUserAndStateAndScheduledTimeAfterOrderByScheduledTimeAsc(
            Long clientId,
            HEADJobState state,
            Instant now
    );

    // Para puntos (si usas COMPLETED * factor)
    long countByClient_IdUserAndState(Long clientId, HEADJobState state);

    long countByClient_IdUserAndStateAndCompletedAtAfter(
            Long clientId, HEADJobState state, Instant after
    );

    long countByClient_IdUser(Long clientId);

    @Query("""
    select p.id as packageId, count(j.id) as total
    from HEADJob j
    join j.request r
    join r.pkg p
    where j.state = :state
      and j.completedAt >= :from
      and p.id in :packageIds
    group by p.id
""")
    List<PackageCountView> countCompletedJobsByPackageIds(
            @Param("state") HEADJobState state,
            @Param("from") Instant from,
            @Param("packageIds") Collection<String> packageIds
    );

    @Query("""
        select count(j.id)
        from HEADJob j
        where j.client.idUser = :clientId
          and j.state = :completedState
    """)
    long countCompletedByClient(
            @Param("clientId") long clientId,
            @Param("completedState") HEADJobState completedState
    );

    // 2) Upcoming: todo lo que NO sea completed/cancelled
    @Query("""
        select count(j.id)
        from HEADJob j
        where j.client.idUser = :clientId
          and j.state not in :terminalStates
    """)
    long countUpcomingByClientExcludingTerminal(
            @Param("clientId") long clientId,
            @Param("terminalStates") Set<HEADJobState> terminalStates
    );

    @Query("""
    select count(distinct j.startAddress)
    from HEADJob j
    where j.client.idUser = :clientId
      and j.startAddress is not null
      and trim(j.startAddress) <> ''
""")
    long countDistinctStartAddressesByClient(@Param("clientId") long clientId);

    @Query("""
    select j.startAddress as address,
           count(j.id) as timesUsed,
           max(j.updatedAt) as lastUsedAt
    from HEADJob j
    where j.client.idUser = :clientId
      and j.startAddress is not null
      and trim(j.startAddress) <> ''
    group by j.startAddress
    order by max(j.updatedAt) desc
""")
    List<HEADLocationAggView> findLocationAggByClient(@Param("clientId") long clientId);

    @EntityGraph(attributePaths = {"request", "request.pkg", "staffUser"})
    Page<HEADJob> findByClient_UuIdUserAndStateInOrderByUpdatedAtDesc(
            String clientUuid,
            Collection<HEADJobState> states,
            Pageable pageable
    );

    @Query("""
  select j
  from HEADJob j
  where j.client.uuIdUser = :clientUuid
    and coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) >= :now
    and j.state not in :terminalStates
  order by coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) asc
""")
    Page<HEADJob> findUpcomingByClientUuid(
            @Param("clientUuid") String clientUuid,
            @Param("now") Instant now,
            @Param("terminalStates") Set<HEADJobState> terminalStates,
            Pageable pageable
    );


    @Query("""
  select j
  from HEADJob j
  where j.client.uuIdUser = :clientUuid
    and (
         coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) < :now
         or j.state in :terminalStates
    )
  order by coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) desc
""")
    Page<HEADJob> findPastByClientUuid(
            @Param("clientUuid") String clientUuid,
            @Param("now") Instant now,
            @Param("terminalStates") Set<HEADJobState> terminalStates,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"request", "request.pkg", "staffUser"})
    Page<HEADJob> findByClient_UuIdUserOrderByUpdatedAtDesc(String clientUuid, Pageable pageable);

    @Query(
            value = """
    select
      j.id as id,
      p.id as packageId,
      p.title as serviceName,
      p.subtitle as categoryLabel,
      cast(j.state as string) as jobState,
      coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) as when,
      case
        when j.serviceMode = 'VIDEO' then 'Videollamada'
        else coalesce(r.startAddress, j.startAddress)
      end as location,
      j.amount as amount,
      j.currency as currency,
      case
        when su is null then null
        else concat(coalesce(su.nombre,''), ' ', coalesce(su.aPaterno,''), ' ', coalesce(su.aMaterno,''))
      end as professionalName,
      concat(
           coalesce(cast(max(oc.nameOccupation) as string), ''),
           case
             when max(op.nameTypeProfile) is null or trim(cast(max(op.nameTypeProfile) as string)) = '' then ''
             when max(oc.nameOccupation) is null or trim(cast(max(oc.nameOccupation) as string)) = '' then ''
             else ' • '
           end,
           coalesce(cast(max(op.nameTypeProfile) as string), '')
         ) as professionalSpecialty,
      rv.rating as rating,
      coalesce(j.cancelNote, '') as notes,
      min(op.IdOccupationProfile) as occupationProfileId,
      max(fa.url) as iconUrl,
      max(fa.tags) as iconTags
    from HEADJob j
    join j.request r
    join r.pkg p
    left join j.staffUser su
    left join j.staffReview rv
    left join HEADPackagesToProfiles ptp
      on ptp.idPackageAvailable.id = p.id
     and ptp.isActive = true
    left join ptp.idOccupationProfile op
    left join op.idOccupation oc
    left join HEADFileAsset fa
      on fa.ownerType = :ownerType
     and fa.ownerId = op.IdOccupationProfile
     and fa.category = :category
     and fa.active = true
    where j.client.uuIdUser = :clientUuid
      and (:statesEmpty = true or j.state in :states)
      and (:profileId is null or op.IdOccupationProfile = :profileId)
      and (
           :q is null or :q = '' or
           lower(p.title) like lower(concat('%', :q, '%')) or
           lower(coalesce(p.subtitle,'')) like lower(concat('%', :q, '%')) or
           lower(coalesce(concat(su.nombre,' ',su.aPaterno,' ',su.aMaterno), '')) like lower(concat('%', :q, '%'))
      )
    group by
      j.id, p.id, p.title, p.subtitle, j.state, j.scheduledTime, j.scheduledAt, j.createdAt,
      j.serviceMode, r.startAddress, j.startAddress,
      j.amount, j.currency,
      su.nombre, su.aPaterno, su.aMaterno,
      rv.rating, j.cancelNote
    order by coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) desc
  """,
            countQuery = """
    select count(distinct j.id)
    from HEADJob j
    join j.request r
    join r.pkg p
    left join j.staffUser su
    left join HEADPackagesToProfiles ptp
      on ptp.idPackageAvailable.id = p.id
     and ptp.isActive = true
    left join ptp.idOccupationProfile op
    where j.client.uuIdUser = :clientUuid
      and (:statesEmpty = true or j.state in :states)
      and (:profileId is null or op.IdOccupationProfile = :profileId)
      and (
           :q is null or :q = '' or
           lower(p.title) like lower(concat('%', :q, '%')) or
           lower(coalesce(p.subtitle,'')) like lower(concat('%', :q, '%')) or
           lower(coalesce(concat(su.nombre,' ',su.aPaterno,' ',su.aMaterno), '')) like lower(concat('%', :q, '%'))
      )
  """
    )
    Page<HEADServiceHistoryRowViewGeneric> findServiceHistoryGenericFiltered(
            @Param("clientUuid") String clientUuid,
            @Param("q") String q,
            @Param("states") Set<HEADJobState> states,
            @Param("statesEmpty") boolean statesEmpty,
            @Param("profileId") Long profileId,
            @Param("ownerType") HEADOwnerType ownerType,
            @Param("category") HEADCategory category,
            Pageable pageable
    );

    long countByClient_UuIdUser(String clientUuid);
    long countByClient_UuIdUserAndState(String clientUuid, HEADJobState state);

    @Query("""
select distinct
  op.IdOccupationProfile as profileId,
  trim(concat(
    coalesce(oc.nameOccupation, ''),
    case
      when op.nameTypeProfile is null or trim(op.nameTypeProfile) = '' then ''
      when oc.nameOccupation is null or trim(oc.nameOccupation) = '' then ''
      else ' '
    end,
    coalesce(op.nameTypeProfile, '')
  )) as profileName
from HEADJob j
join j.request r
join r.pkg p
join HEADPackagesToProfiles ptp on ptp.idPackageAvailable.id = p.id
join ptp.idOccupationProfile op
left join op.idOccupation oc
where j.client.uuIdUser = :clientUuid
  and ptp.isActive = true
  and p.active = true
order by profileName asc
""")
    List<HEADServiceTypeChipView> findProfileChipsForClient(@Param("clientUuid") String clientUuid);


    @Query("""
      select count(j.id)
      from HEADJob j
      where j.client.uuIdUser = :clientUuid
        and j.state in :states
    """)
    long countByClientUuidAndStates(@Param("clientUuid") String clientUuid,
                                    @Param("states") Set<HEADJobState> states);

    @Query("""
  select max(j.completedAt)
  from HEADJob j
  where j.client.idUser = :clientId
    and j.state = 'COMPLETED'
""")
    Optional<Instant> findLastCompletedAt(@Param("clientId") Long clientId);
    long countByClient_UuIdUserAndStateAndCompletedAtAfter(
            String clientUuid, HEADJobState state, Instant after
    );

    @Query("""
    select count(distinct j.client.idUser)
    from HEADJob j
    where j.staffUser.idUser = :staffId
      and j.state = :state
""")
    Long countDistinctPatientsByStaff(@Param("staffId") Long staffId,
                                      @Param("state") HEADJobState state);

    @Query("""
    select count(j.id)
    from HEADJob j
    where j.staffUser.idUser = :staffId
      and j.state = :state
""")
    Long countCompletedServicesByStaff(@Param("staffId") Long staffId,
                                       @Param("state") HEADJobState state);

    @Query(value = """
    select avg(timestampdiff(minute, j.assigned_at, j.accepted_at))
    from head_job j
    where j.staff_user_id = :staffId
      and j.assigned_at is not null
      and j.accepted_at is not null
""", nativeQuery = true)
    Double avgResponseMinutesByStaff(@Param("staffId") Long staffId);

    @Query("""
    select
        j.id as id,
        p.title as serviceName,
        concat(
            coalesce(c.nombre, ''),
            case
                when c.aPaterno is not null and trim(c.aPaterno) <> ''
                    then concat(' ', c.aPaterno)
                else ''
            end
        ) as patientName,
        coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) as when,
        case
            when j.serviceMode = 'VIDEO'
                then 'Videollamada'
            else coalesce(j.startAddress, '')
        end as address,
        coalesce(j.durationMinBucket, 0) as durationMinutes,
        cast(j.state as string) as jobState,
        cast(j.serviceMode as string) as serviceMode,
        coalesce(p.subtitle, '') as serviceDescription,
        j.clientLat as lat,
        j.clientLng as lng
    from HEADJob j
    join j.request r
    join r.pkg p
    join j.client c
    where j.staffUser.idUser = :staffId
      and coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) >= :from
      and coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) < :to
      and j.state in :states
    order by coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) asc
""")
    List<HEADScheduledServiceRowView> findScheduledServicesForStaff(
            @Param("staffId") Long staffId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("states") Set<HEADJobState> states
    );

    @Query("""
    select
        j.id as id,
        p.title as serviceName,
        concat(
            coalesce(c.nombre, ''),
            case
                when c.aPaterno is not null and trim(c.aPaterno) <> ''
                    then concat(' ', c.aPaterno)
                else ''
            end
        ) as patientName,
        coalesce(j.scheduledTime, j.scheduledAt, j.createdAt) as when,
        case
            when j.serviceMode = 'VIDEO'
                then 'Videollamada'
            else coalesce(j.startAddress, '')
        end as address,
        coalesce(j.durationMinBucket, 0) as durationMinutes,
        cast(j.state as string) as jobState,
        cast(j.serviceMode as string) as serviceMode,
        coalesce(p.subtitle, '') as serviceDescription,
        j.clientLat as lat,
        j.clientLng as lng
    from HEADJob j
    join j.request r
    join r.pkg p
    join j.client c
    where j.id = :jobId
      and j.staffUser.idUser = :staffId
""")
    Optional<HEADScheduledServiceRowView> findScheduledServiceDetailForStaff(
            @Param("jobId") Long jobId,
            @Param("staffId") Long staffId
    );

    @Query("""
    select
        j.id as id,
        concat(
            coalesce(c.nombre, ''),
            case
                when c.aPaterno is not null and trim(c.aPaterno) <> ''
                    then concat(' ', c.aPaterno)
                else ''
            end
        ) as patientName,
        p.title as serviceName,
        coalesce(j.startAddress, '') as address,
        coalesce(j.completedAt, j.cancelledAt, j.updatedAt) as completedAt,
        coalesce(j.durationMinBucket, 0) as durationMinutes,
        coalesce(f.staffPayoutAmount, 0) as amount,
        cast(j.state as string) as jobState,
        cast(j.serviceMode as string) as serviceMode
    from HEADJob j
    join j.request r
    join r.pkg p
    join j.client c
    left join HEADJobFinancial f on f.job.id = j.id
    where j.staffUser.idUser = :staffId
      and j.state in :states
    order by coalesce(j.completedAt, j.cancelledAt, j.updatedAt) desc
""")
    List<HEADCompletedServiceRowView> findServiceHistoryForStaff(
            @Param("staffId") Long staffId,
            @Param("states") Set<HEADJobState> states
    );

    @Query("""
    select
        count(j.id) as totalServices,
        coalesce(sum(f.staffPayoutAmount), 0) as totalEarned
    from HEADJob j
    left join HEADJobFinancial f on f.job.id = j.id
    where j.staffUser.idUser = :staffId
      and j.state = :completedState
""")
    HEADServiceHistorySummaryView getCompletedServiceHistorySummaryForStaff(
            @Param("staffId") Long staffId,
            @Param("completedState") HEADJobState completedState
    );

    List<HEADJob> findTop100ByStateInAndScheduledTimeBetweenAndScheduleReminderSentFalse(
            Collection<HEADJobState> states,
            Instant from,
            Instant to
    );
}