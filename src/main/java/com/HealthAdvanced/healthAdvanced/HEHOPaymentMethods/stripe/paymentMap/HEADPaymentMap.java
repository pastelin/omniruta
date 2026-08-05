package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.paymentMap;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClientToCustomer;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HEADPaymentMap {
    public HEADClientToCustomer headClientToCustomer(HEADClients headClients) {
        Map<String, Object> customerParams = new HashMap<>();
        var headClientToCustomers = new HEADClientToCustomer();
        try {
            customerParams.put("email", headClients.getEmail());
            Customer customer = Customer.create(customerParams);
            headClientToCustomers.setIdClient(headClients);
            headClientToCustomers.setCustomerId(customer.getId());
        } catch (StripeException e) {
            headClientToCustomers = null;
        }
        return headClientToCustomers;
    }
}
