package com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.mapper.HEADStripeAccountUiMapper;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.stripe.model.Account;
import com.stripe.model.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HEADStaffStripeUiStatusService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;
    private final HEADStripeAccountUiMapper stripeAccountUiMapper;

    public HEADStripeAccountUiResponse getCurrentUiStatus() {
        String staffUuid = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADStaffToStripeAccount rel = staffToStripeAccountRepository.findByStaffUser(staff)
                .orElse(null);

        if (rel == null) {
            return stripeAccountUiMapper.empty();
        }

        HEADPayoutMethodSummary payoutMethod = resolvePayoutMethod(rel);

        return stripeAccountUiMapper.toResponse(
                rel,
                payoutMethod.bankName(),
                payoutMethod.last4()
        );
    }

    private HEADPayoutMethodSummary resolvePayoutMethod(HEADStaffToStripeAccount rel) {
        try {
            if (rel.getConnectedAccountId() == null || rel.getConnectedAccountId().isBlank()) {
                return new HEADPayoutMethodSummary(null, null);
            }

            Account account = Account.retrieve(rel.getConnectedAccountId());

            if (account.getExternalAccounts() == null
                    || account.getExternalAccounts().getData() == null
                    || account.getExternalAccounts().getData().isEmpty()) {
                return new HEADPayoutMethodSummary(null, null);
            }

            String defaultExternalAccountId = rel.getDefaultExternalAccountId();

            BankAccount selected = account.getExternalAccounts()
                    .getData()
                    .stream()
                    .filter(BankAccount.class::isInstance)
                    .map(BankAccount.class::cast)
                    .filter(acc -> defaultExternalAccountId == null
                            || defaultExternalAccountId.isBlank()
                            || defaultExternalAccountId.equals(acc.getId()))
                    .findFirst()
                    .orElseGet(() ->
                            account.getExternalAccounts()
                                    .getData()
                                    .stream()
                                    .filter(BankAccount.class::isInstance)
                                    .map(BankAccount.class::cast)
                                    .findFirst()
                                    .orElse(null)
                    );

            if (selected == null) {
                return new HEADPayoutMethodSummary(null, null);
            }

            return new HEADPayoutMethodSummary(
                    selected.getBankName(),
                    selected.getLast4()
            );

        } catch (Exception e) {
            return new HEADPayoutMethodSummary(null, null);
        }
    }

    private record HEADPayoutMethodSummary(
            String bankName,
            String last4
    ) {}
}