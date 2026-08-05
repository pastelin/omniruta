package com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADStripeProperties;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class HEADStripeConnectRedirectService {

    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;
    private final HEADStripeProperties stripeProperties;
    private final HEADStripeAccountRealtimeService stripeAccountRealtimeService;

    public String buildRefreshUrl(String connectedAccountId) {
        return UriComponentsBuilder
                .fromHttpUrl(stripeProperties.connect().refreshUrl())
                .queryParam("account", connectedAccountId)
                .build(true)
                .toUriString();
    }

    public String buildReturnUrl(String connectedAccountId) {
        return UriComponentsBuilder
                .fromHttpUrl(stripeProperties.connect().returnUrl())
                .queryParam("account", connectedAccountId)
                .build(true)
                .toUriString();
    }

    @Transactional
    public String handleRefreshAndReturnUrl(String connectedAccountId) throws StripeException {
        HEADStaffToStripeAccount rel = staffToStripeAccountRepository
                .findByConnectedAccountId(connectedAccountId)
                .orElseThrow(() -> new HEADBadRequestException("No existe relación staff ↔ Stripe account"));

        rel.setStripeStatus(HEADStripeAccountStatus.ONBOARDING);
        rel = staffToStripeAccountRepository.save(rel);

        stripeAccountRealtimeService.emitAfterCommit(rel);

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(rel.getConnectedAccountId())
                .setRefreshUrl(buildRefreshUrl(rel.getConnectedAccountId()))
                .setReturnUrl(buildReturnUrl(rel.getConnectedAccountId()))
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        return accountLink.getUrl();
    }

    @Transactional
    public HEADStaffToStripeAccount handleReturnAndSync(String connectedAccountId) throws StripeException {
        HEADStaffToStripeAccount rel = staffToStripeAccountRepository
                .findByConnectedAccountId(connectedAccountId)
                .orElseThrow(() -> new HEADBadRequestException("No existe relación staff ↔ Stripe account"));

        Account account = Account.retrieve(rel.getConnectedAccountId());

        rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
        rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
        rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));

        if (!Boolean.TRUE.equals(account.getDetailsSubmitted())) {
            rel.setStripeStatus(HEADStripeAccountStatus.ONBOARDING);
        } else if (Boolean.TRUE.equals(account.getPayoutsEnabled())) {
            rel.setStripeStatus(HEADStripeAccountStatus.VERIFIED);
        } else {
            rel.setStripeStatus(HEADStripeAccountStatus.PENDING);
        }

        rel = staffToStripeAccountRepository.save(rel);

        stripeAccountRealtimeService.emitAfterCommit(rel);

        return rel;
    }
}