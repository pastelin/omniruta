package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.dtos.HEADPromoDTO;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADPackageAvailableDto {
    private String id;
    private String title;
    private String subtitle;
    private BigDecimal minPriceFrom;
    private String currency;
    private String iconUrl;
    private Boolean isPopular;
    private Integer duration;
    private Boolean isRequiredPrescription;
    private List<HEADPackageOptionAvailableDto> options;
}