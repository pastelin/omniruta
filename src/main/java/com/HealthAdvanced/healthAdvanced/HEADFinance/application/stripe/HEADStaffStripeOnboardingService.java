package com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADStripeProperties;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeOnboardingLinkResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADStaffStripeOnboardingService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;
    private final HEADStripeProperties stripeProperties;
    private final HEADStripeConnectRedirectService stripeConnectRedirectService;
    private final HEADStripeAccountRealtimeService stripeAccountRealtimeService;

    @Transactional
    public HEADStripeOnboardingLinkResponse createOrRefreshOnboardingLink() throws StripeException {
        HEADPersonalUser staff = getCurrentStaff();

        HEADStaffToStripeAccount rel = staffToStripeAccountRepository.findByStaffUser(staff)
                .orElseGet(() -> createConnectedAccountForStaff(staff));

        syncStripeAccountStatus(rel);

        // Mientras el usuario entra al onboarding, la UI lo puede mostrar como LOADING
        if (!Boolean.TRUE.equals(rel.getPayoutsEnabled())) {
            rel.setStripeStatus(HEADStripeAccountStatus.ONBOARDING);
            rel = staffToStripeAccountRepository.save(rel);

            // EMIT 1
            stripeAccountRealtimeService.emitAfterCommit(rel);
        }

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(rel.getConnectedAccountId())
                .setRefreshUrl(stripeConnectRedirectService.buildRefreshUrl(rel.getConnectedAccountId()))
                .setReturnUrl(stripeConnectRedirectService.buildReturnUrl(rel.getConnectedAccountId()))
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);

        return new HEADStripeOnboardingLinkResponse(accountLink.getUrl());
    }

    @Transactional
    public HEADStripeAccountStatusResponse getCurrentStaffStripeStatus() throws StripeException {
        HEADPersonalUser staff = getCurrentStaff();

        HEADStaffToStripeAccount rel = staffToStripeAccountRepository.findByStaffUser(staff)
                .orElseThrow(() -> new HEADBadRequestException("El staff aún no tiene cuenta conectada en Stripe"));

        syncStripeAccountStatus(rel);

        return new HEADStripeAccountStatusResponse(
                rel.getConnectedAccountId(),
                Boolean.TRUE.equals(rel.getDetailsSubmitted()),
                Boolean.TRUE.equals(rel.getPayoutsEnabled()),
                Boolean.TRUE.equals(rel.getChargesEnabled())
        );
    }

    private HEADStaffToStripeAccount createConnectedAccountForStaff(HEADPersonalUser staff) {
        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("MX")
                    .setEmail(staff.getEmail())
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                    .setBusinessProfile(
                            AccountCreateParams.BusinessProfile.builder()
                                    .setProductDescription(
                                            "Plataforma de servicios profesionales a domicilio y bajo demanda, solicitados mediante aplicación móvil."
                                    )
                                    .build()
                    )
                    .build();

            Account account = Account.create(params);

            HEADStaffToStripeAccount rel = new HEADStaffToStripeAccount();
            rel.setStaffUser(staff);
            rel.setConnectedAccountId(account.getId());
            rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
            rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
            rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
            rel.setDefaultExternalAccountId(null);
            rel.setDefaultExternalAccountType(null);
            rel.setStripeStatus(HEADStripeAccountStatus.ONBOARDING);

            return staffToStripeAccountRepository.save(rel);

        } catch (StripeException e) {
            throw new HEADBadRequestException("No se pudo crear la cuenta conectada en Stripe: " + e.getMessage());
        }
    }

    private void syncStripeAccountStatus(HEADStaffToStripeAccount rel) throws StripeException {
        Account account = Account.retrieve(rel.getConnectedAccountId());

        rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
        rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
        rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));

        // Aquí usa tu lógica real de resolveStatus(...)
        if (!Boolean.TRUE.equals(account.getDetailsSubmitted())) {
            rel.setStripeStatus(HEADStripeAccountStatus.ONBOARDING);
        } else if (Boolean.TRUE.equals(account.getPayoutsEnabled())) {
            rel.setStripeStatus(HEADStripeAccountStatus.VERIFIED);
        } else {
            rel.setStripeStatus(HEADStripeAccountStatus.PENDING);
        }

        staffToStripeAccountRepository.save(rel);
    }

    private HEADPersonalUser getCurrentStaff() {
        String staffUuid = jwt.getUserNamePersonalUser();

        return personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));
    }
}