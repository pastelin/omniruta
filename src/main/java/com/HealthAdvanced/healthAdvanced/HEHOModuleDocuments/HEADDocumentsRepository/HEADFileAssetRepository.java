package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HEADFileAssetRepository extends JpaRepository<HEADFileAsset, Long> {
    List<HEADFileAsset> findByOwnerTypeAndCategoryAndActive(
            HEADOwnerType ownerType, HEADCategory category, boolean active);

    List<HEADFileAsset> findByOwnerTypeAndOwnerIdAndActive(
            HEADOwnerType ownerType, Long ownerId, boolean active);

    List<HEADFileAsset> findByOwnerTypeAndActive(
            HEADOwnerType ownerType, boolean active);

    List<HEADFileAsset> findByCategoryAndActiveOrderBySortOrderAsc(HEADCategory category, boolean active);

    Optional<HEADFileAsset> findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
            HEADOwnerType ownerType,
            Long ownerId,
            HEADCategory category
    );

    @Query("""
    SELECT f.url
      FROM HEADFileAsset f
     WHERE f.active = true
       AND f.visibility = :visibility
       AND f.category  = :category
       AND (
             f.tags = :tag
          OR f.tags LIKE CONCAT(:tag, ',%')
          OR f.tags LIKE CONCAT('%,', :tag, ',%')
          OR f.tags LIKE CONCAT('%,', :tag)
       )
     ORDER BY f.sortOrder ASC, f.id DESC
  """)
    List<String> findUrlsByCategoryAndTag(
            @Param("category") HEADCategory category,
            @Param("tag") String tag,
            @Param("visibility") HEADVisibility visibility
    );

    @Query("""
    SELECT f.url
      FROM HEADFileAsset f
     WHERE f.active = true
       AND f.visibility = :visibility
       AND f.category  = :category
     ORDER BY f.sortOrder ASC, f.id DESC
  """)
    List<String> findUrlsByCategory(
            @Param("category") HEADCategory category,
            @Param("visibility") HEADVisibility visibility
    );

    @Query("""
    SELECT f.id
    FROM HEADFileAsset f
    WHERE f.storageKey = :storageKey
      AND f.ownerId = :idUser
      AND f.active = true
    """)
    List<Long> findIdByStorageKey(@Param("storageKey") String storageKey,
                                  @Param("idUser") Long idUser);


    List<HEADFileAsset> findByOwnerTypeAndOwnerIdAndCategoryAndActive(
            HEADOwnerType ownerType,
            Long ownerId,
            HEADCategory category,
            boolean active
    );

    List<HEADFileAsset> findByOwnerTypeAndOwnerIdAndCategoryAndDocumentCatalogueAndActive(
            HEADOwnerType ownerType,
            Long ownerId,
            HEADCategory category,
            Integer documentCatalogue,
            boolean active
    );
}
