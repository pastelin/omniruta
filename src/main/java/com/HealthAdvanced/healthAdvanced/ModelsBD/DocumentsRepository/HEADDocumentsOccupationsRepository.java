package com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository;

import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentOccupations;
import com.sun.mail.imap.protocol.ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HEADDocumentsOccupationsRepository extends JpaRepository<HEADDocumentOccupations, Long> {
    @Query("""
    select o.headDocumentCatalogue.idDocument
    from HEADDocumentOccupations o
    where o.headOccupationProfile.IdOccupationProfile = :occProfileId
  """)
    Set<Integer> findRequiredDocIdsByOccProfile(@Param("occProfileId") Long occProfileId);

    @Query("""
      select distinct o.headDocumentCatalogue
      from HEADDocumentOccupations o
      where o.headOccupationProfile.IdOccupationProfile in :occProfileIds
    """)
    List<HEADDocumentCatalogue> findRequiredCatalogsByOccProfiles(@Param("occProfileIds") List<Long> occProfileIds);

    @Query("""
      select o
      from HEADDocumentOccupations o
      where o.headOccupationProfile.IdOccupationProfile = :occProfileId
    """)
    List<HEADDocumentOccupations> findRequiredCatalogsByOccProfilesIds(@Param("occProfileId") Long occProfileIds);

    @Query("""
        select d
        from HEADDocumentOccupations d
        where d.headOccupationProfile.IdOccupationProfile = :occupationProfileId
          and d.headDocumentCatalogue.idDocument = :documentId
    """)
    Optional<HEADDocumentOccupations> findDocOccupation(
            @Param("occupationProfileId") Long occupationProfileId,
            @Param("documentId") Integer documentId
    );
}
