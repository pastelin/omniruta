package com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.interfaces.HEADDocLightView;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HEADDocumentsRepository extends JpaRepository<HEADDocuments, Long> {

    @Query("""
      select d from HEADDocuments d
      where d.idUser.idUser = :idUser
    """)
    List<HEADDocuments> findByIdUser(@Param("idUser") Long idUser);

    @Query("""
      select d from HEADDocuments d
      where d.idUser.idUser = :userId
        and d.active = true
        and (:occProfileId is null and d.occupationProfile is null
             or :occProfileId is not null and d.occupationProfile.IdOccupationProfile = :occProfileId)
    """)
    List<HEADDocuments> findActiveByUserAndOccProfile(@Param("userId") Long userId,
                                                      @Param("occProfileId") Long occProfileId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update HEADDocuments d
         set d.active = false
       where d.idUser.idUser = :userId
         and d.idDocument.idDocument = :docId
         and d.active = true
         and (:occProfileId is null and d.occupationProfile is null
              or :occProfileId is not null and d.occupationProfile.IdOccupationProfile = :occProfileId)
    """)
    int deactivatePreviousForSameSlot(@Param("userId") Long userId,
                                      @Param("docId") Integer docId,
                                      @Param("occProfileId") Long occProfileId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update HEADDocuments d
         set d.status = :status,
             d.reviewNotes = :motiveNote
       where d.idUser.idUser = :userId
         and d.idDocument.idDocument = :docId
         and d.active = true
         and (:occProfileId is null and d.occupationProfile is null
              or :occProfileId is not null and d.occupationProfile.IdOccupationProfile = :occProfileId)
    """)
    int updateStatusForIdDocument(@Param("userId") Long userId,
                                  @Param("docId") Integer docId,
                                  @Param("occProfileId") Long occProfileId,
                                  @Param("status") HEADDocumentStatus status,
                                  @Param("motiveNote") String motiveNote);

    @Query("""
      select distinct d.idDocument.idDocument
      from HEADDocuments d
      where d.idUser.idUser = :userId
        and d.active = true
        and d.status = 'APPROVED'
        and (:occProfileId is null and d.occupationProfile is null
             or :occProfileId is not null and d.occupationProfile.IdOccupationProfile = :occProfileId)
    """)
    Set<Integer> findApprovedDocIdsByUserAndOccProfile(@Param("userId") Long userId,
                                                       @Param("occProfileId") Long occProfileId);

    @Query("""
    select d
      from HEADDocuments d
     where d.idUser.idUser = :userId
       and d.active = true
       and (:occProfileId is null and d.occupationProfile is null
            or :occProfileId is not null and d.occupationProfile.IdOccupationProfile = :occProfileId)
       and d.idDocument.idDocument in :docIds
  """)
    List<HEADDocuments> findActiveByUserOccProfileAndDocIds(@Param("userId") Long userId,
                                                            @Param("occProfileId") Long occProfileId,
                                                            @Param("docIds") Collection<Integer> docIds);

    @Query("""
       select d.idDocs as idDocs, d.mimeType as mimeType, d.extension as extension
       from HEADDocuments d
       where d.idDocs = :id
    """)
    Optional<HEADDocLightView> findDocByIdLight(@Param("id") Long id);

    @Query("""
    SELECT d.idDocs
    FROM HEADDocuments d
    WHERE d.storageKey = :storageKey
      AND d.idUser.idUser = :userId
    """)
    List<Long> findIdDocumentByStorageKeyAndIdUser(@Param("storageKey") String storageKey,
                                                   @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM HEADDocuments d WHERE d.fileAsset.id IN :assetIds")
    void deleteByFileAssetIds(@Param("assetIds") Collection<Long> assetIds);

    @Query("""
    select d
    from HEADDocuments d
    where d.idUser.idUser = :userId
      and d.idDocument.idDocument = :documentId
      and (
            (:occProfileId is null and d.occupationProfile is null)
            or d.occupationProfile.IdOccupationProfile = :occProfileId
          )
      and d.active = true
    order by d.idDocs desc
""")
    List<HEADDocuments> findActiveBySlot(
            @Param("userId") Long userId,
            @Param("documentId") Integer documentId,
            @Param("occProfileId") Long occProfileId
    );

    long countByIdUser_IdUserAndActiveTrue(Long userId);

    long countByIdUser_IdUserAndActiveTrueAndStatus(Long userId, HEADDocumentStatus status);

    @Query("""
    select max(d.uploadedAt)
    from HEADDocuments d
    where d.idUser.idUser = :userId
      and d.active = true
""")
    LocalDateTime findLastUploadedAtByUser(@Param("userId") Long userId);

    @Query("""
    select d
    from HEADDocuments d
    join fetch d.idDocument dc
    left join fetch d.occupationProfile op
    where d.idUser.idUser = :userId
      and d.active = true
      and (:status is null or d.status = :status)
      and (
            (:occProfileId is null)
            or (op is not null and op.IdOccupationProfile = :occProfileId)
          )
    order by d.uploadedAt desc, d.idDocs desc
""")
    List<HEADDocuments> findAdminDocumentDetailByUser(
            @Param("userId") Long userId,
            @Param("status") HEADDocumentStatus status,
            @Param("occProfileId") Long occProfileId
    );
}

