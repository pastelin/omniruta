package com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADCredentialType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADStaffCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HEADStaffCredentialRepository extends JpaRepository<HEADStaffCredential, Long> {

    @Query("""
select c
from HEADStaffCredential c
where c.staffUser.idUser = :staffUserId
  and c.occupationProfile.IdOccupationProfile = :occProfileId
  and c.credentialType = :type
""")
    Optional<HEADStaffCredential> findCredential(
            @Param("staffUserId") Long staffUserId,
            @Param("occProfileId") Long occProfileId,
            @Param("type") HEADCredentialType type
    );

    Optional<HEADStaffCredential> findByStaffUser_IdUserAndOccupationProfileIsNullAndCredentialType(
            Long staffUserId,
            HEADCredentialType type
    );


    @Query("""
select c.value
  from HEADStaffCredential c
 where c.staffUser.idUser = :staffUserId
   and c.credentialType = :type
   and c.status = 'APPROVED'
   and c.occupationProfile.IdOccupationProfile = :occProfileId
""")
    Optional<String> findApprovedValueByStaffAndOccProfile(
            @Param("staffUserId") Long staffUserId,
            @Param("occProfileId") Long occProfileId,
            @Param("type") HEADCredentialType type
    );

    @Query("""
select c.value
  from HEADStaffCredential c
  join c.occupationProfile op
  join op.idOccupation occ
 where c.staffUser.idUser = :staffUserId
   and c.credentialType = :type
   and c.status = 'APPROVED'
   and occ.code = :occupationCode
 order by c.updatedAt desc
""")
    List<String> findApprovedValueByStaffAndOccupationCode(
            @Param("staffUserId") Long staffUserId,
            @Param("type") HEADCredentialType type,
            @Param("occupationCode") HEADOccupationCode occupationCode
    );



    // Fallback: value aprobado "global" (occupationProfile null)
    @Query("""
        select c.value
          from HEADStaffCredential c
         where c.staffUser.idUser = :staffUserId
           and c.credentialType = :type
           and c.status = 'APPROVED'
           and c.occupationProfile is null
    """)
    Optional<String> findApprovedValueGlobal(
            @Param("staffUserId") Long staffUserId,
            @Param("type") HEADCredentialType type
    );

    // Si quieres un método que ya haga fallback en 1 query:
    // - primero intenta el perfil exacto
    // - si no existe, cae al null
    @Query("""
        select c.value
          from HEADStaffCredential c
         where c.staffUser.idUser = :staffUserId
           and c.credentialType = :type
           and c.status = 'APPROVED'
           and (
                (:occProfileId is not null and c.occupationProfile.IdOccupationProfile = :occProfileId)
                or (:occProfileId is null and c.occupationProfile is null)
                or (c.occupationProfile is null)
           )
         order by
            case
              when :occProfileId is not null and c.occupationProfile.IdOccupationProfile = :occProfileId then 0
              else 1
            end
    """)
    List<String> findApprovedValueWithFallback(
            @Param("staffUserId") Long staffUserId,
            @Param("occProfileId") Long occProfileId,
            @Param("type") HEADCredentialType type
    );

    // Actualizar status + auditoría (cuando admin aprueba/rechaza)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update HEADStaffCredential c
           set c.status = :status,
               c.reviewedByAdminId = :adminId,
               c.reviewedAt = CURRENT_TIMESTAMP,
               c.updatedAt = CURRENT_TIMESTAMP
         where c.id = :id
    """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") HEADDocumentStatus status,
            @Param("adminId") Long adminId
    );

    @Query("""
        select c.value
          from HEADStaffCredential c
         where c.staffUser.idUser = :staffUserId
           and c.occupationProfile is null
           and c.credentialType = 'LICENSE_NO'
           and c.status = 'APPROVED'
    """)
    Optional<String> findApprovedGlobalLicenseNo(@Param("staffUserId") Long staffUserId);
}

