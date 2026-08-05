package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "clientToCustomer")
public class HEADClientToCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClientToCustomer;
    @ManyToOne
    private HEADClients idClient;
    private String customerId;
    @Column(name = "default_payment_method_id")
    private String defaultPaymentMethodId;
    @Column(name = "payment_status")
    private HEADPaymentStatus paymentStatus;
}
