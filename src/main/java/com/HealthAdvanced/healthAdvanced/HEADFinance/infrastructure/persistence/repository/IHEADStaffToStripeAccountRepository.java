package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IHEADStaffToStripeAccountRepository extends JpaRepository<HEADStaffToStripeAccount, Long> {

    Optional<HEADStaffToStripeAccount> findByStaffUser(HEADPersonalUser staffUser);

    Optional<HEADStaffToStripeAccount> findByConnectedAccountId(String connectedAccountId);
}