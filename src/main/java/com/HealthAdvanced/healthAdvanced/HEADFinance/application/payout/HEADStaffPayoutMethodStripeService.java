package com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADDefaultPayoutMethodDto;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADDefaultPayoutMethodResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeExternalAccountType;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.ExternalAccount;
import com.stripe.param.AccountExternalAccountListParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.stripe.exception.StripeException;
import com.stripe.model.BankAccount;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HEADStaffPayoutMethodStripeService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository personalUserRepository;
    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;

    public HEADDefaultPayoutMethodResponse getDefaultPayoutMethodForCurrentStaff() throws StripeException {
        String uuid = jwt.getUserNamePersonalUser();

        HEADPersonalUser staff = personalUserRepository.findByUidUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADStaffToStripeAccount rel = staffToStripeAccountRepository.findByStaffUser(staff)
                .orElseThrow(() -> new HEADBadRequestException("El staff no tiene connected account en Stripe"));

        String connectedAccountId = rel.getConnectedAccountId();
        if (connectedAccountId == null || connectedAccountId.isBlank()) {
            throw new HEADBadRequestException("El staff no tiene connectedAccountId en Stripe");
        }

        Account account = Account.retrieve(connectedAccountId);

        if (account.getExternalAccounts() == null
                || account.getExternalAccounts().getData() == null
                || account.getExternalAccounts().getData().isEmpty()) {
            return new HEADDefaultPayoutMethodResponse(true, false, null);
        }

        String defaultExternalAccountId = rel.getDefaultExternalAccountId();

        BankAccount selected = account.getExternalAccounts()
                .getData()
                .stream()
                .filter(BankAccount.class::isInstance)
                .map(BankAccount.class::cast)
                .filter(acc -> defaultExternalAccountId == null || defaultExternalAccountId.isBlank()
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
            return new HEADDefaultPayoutMethodResponse(true, false, null);
        }

        if (defaultExternalAccountId == null || defaultExternalAccountId.isBlank()) {
            rel.setDefaultExternalAccountId(selected.getId());
            staffToStripeAccountRepository.save(rel);
        }

        HEADDefaultPayoutMethodDto dto = new HEADDefaultPayoutMethodDto(
                selected.getId(),
                "BANK_ACCOUNT",
                selected.getBankName() != null ? selected.getBankName() : "Banco",
                selected.getLast4() != null ? selected.getLast4() : "0000",
                true
        );

        return new HEADDefaultPayoutMethodResponse(true, true, dto);
    }
}