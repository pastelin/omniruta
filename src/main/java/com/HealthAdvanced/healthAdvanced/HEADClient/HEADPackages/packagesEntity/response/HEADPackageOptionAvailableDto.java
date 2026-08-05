package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.dtos.HEADPromoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADPackageOptionAvailableDto {
    private Long id;
    private String optionLabel;
    private Boolean includesMaterials;
    private BigDecimal priceFrom;
    private BigDecimal crossedPrice;
    private String currency;
    private String promoSource;
    private Integer discountPercent;
    private HEADPromoDTO promo;
}
