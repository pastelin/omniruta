package com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffMenuProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetStaffMenuProfileService {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository staffRepository;
    private final HEADOccupationsProfilesRepository occupationProfileRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final HEADFileAssetRepository headFileAssetRepository;
    private final HEADOccupationPersonalUserRepository occProfileRepo;

    public HEADStaffMenuProfileResponse execute() {
        String staffUuid = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = staffRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        Instant from = LocalDate.now(MX_ZONE).atStartOfDay(MX_ZONE).toInstant();
        Instant to = LocalDate.now(MX_ZONE).plusDays(1).atStartOfDay(MX_ZONE).toInstant();

        BigDecimal todayEarnings = jobFinancialRepository.sumStaffPayoutByRange(
                staff.getIdUser(),
                from,
                to
        );

        var occCode = occProfileRepo.findPrimaryOccupationCodeOrNull(staff.getIdUser());

        var nameStaff = HEADNameFormatters.buildStaffNameLastName(staff, occCode);

        String role = staffRepository.findPrimaryOccupationProfileId(staff.getIdUser())
                .flatMap(occupationProfileRepository::findById)
                .map(this::buildRole)
                .orElse("");

        String avatar = resolveStaffAvatarUrl(staff.getIdUser());

        return new HEADStaffMenuProfileResponse(
                nameStaff,
                role,
                avatar,
                formatMoney(todayEarnings)
        );
    }

    private String buildRole(HEADOccupationProfile profile) {
        String occupation = Optional.ofNullable(profile.getIdOccupation())
                .map(HEADOccupations::getNameOccupation)
                .orElse("");

        return Optional.ofNullable(profile.getNameTypeProfile()).orElse(occupation);
    }

    private String formatMoney(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return String.format(Locale.US, "$%,.2f", safe);
    }

    private String resolveStaffAvatarUrl(Long staffUserId) {
        return headFileAssetRepository
                .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                        HEADOwnerType.STAFF,
                        staffUserId,
                        HEADCategory.AVATAR
                )
                .map(HEADFileAsset::getUrl)
                .orElse("");
    }
}