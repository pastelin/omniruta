package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "serviceRequestClient")
public class HEADServiceRequestClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServiceRequestClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", referencedColumnName = "id", nullable = false)
    private HEADPackagesPersonal pkg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_option_id", nullable = false)
    private HEADPackageOption packageOption;

    @ManyToOne
    private HEADClients idClient;

    private Double latitude;
    private Double longitude;
    private String dateCurrent;
    private String uuIdUser;
    private String tokenNotification;
    private String startAddress;
    private String endAddress;
    private BigDecimal amount;
    private String currency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_asset_id")
    private HEADFileAsset prescriptionAsset;
    private Long idProfile;
}
