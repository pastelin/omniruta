package com.HealthAdvanced.healthAdvanced.ModelsBD.repositories;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClientToCustomer;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IHEADClientToCustomerRepository extends JpaRepository<HEADClientToCustomer, Long> {
    Optional<HEADClientToCustomer> findByIdClient(HEADClients idUser);
}
