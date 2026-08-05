package com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface HEADClientWebSocketRepository extends JpaRepository<HEADServiceRequestClient, Long> {

    Optional<HEADServiceRequestClient> findByIdClient(HEADClients idClient);

    @Query("""
        select sr
        from HEADServiceRequestClient sr
        join fetch sr.pkg p
        join fetch sr.packageOption po
        where sr.idServiceRequestClient = :requestId
    """)
    Optional<HEADServiceRequestClient> findByIdWithPackageAndOption(@Param("requestId") Long requestId);

    @Query("""
        select sr
        from HEADServiceRequestClient sr
        join fetch sr.pkg p
        join fetch sr.packageOption po
        where sr.idClient = :client
    """)
    Optional<HEADServiceRequestClient> findByIdClientWithPackageAndOption(@Param("client") HEADClients client);

    @Query("""
        SELECT u
        FROM HEADServiceRequestClient u
        WHERE (6371 * acos(
            cos(radians(:latitude)) * cos(radians(u.latitude)) *
            cos(radians(u.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(u.latitude))
        )) < :radio
    """)
    Stream<HEADServiceRequestClient> findServiceRequestClientRadio(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radio") double radio
    );

    @Modifying
    @Query("""
        update HEADServiceRequestClient sr
           set sr.prescriptionAsset = null
         where sr.prescriptionAsset.id = :assetId
    """)
    int clearPrescriptionAssetByAssetId(@Param("assetId") Long assetId);
}