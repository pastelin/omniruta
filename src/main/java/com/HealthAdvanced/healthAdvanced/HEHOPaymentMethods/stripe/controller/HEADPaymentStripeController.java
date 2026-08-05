package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADErrorCommonsSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService.HEADPaymentStripeService;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentMethodDeleteRequest;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentMethodRequest;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentStripeAmountRequest;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response.HEADPaymentStripeResponse;
import com.mysql.cj.PreparedQuery;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class HEADPaymentStripeController {

    private final HEADPaymentStripeService paymentService;

    @PostMapping("/payment-methods/attach")
    public ResponseEntity<?> attachPaymentMethod(@RequestBody HEADPaymentMethodRequest req) {
        paymentService.attachPaymentMethod(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/payment-methods/{pmId}/default")
    public ResponseEntity<?> setDefault(@PathVariable String pmId) throws StripeException {
        paymentService.setDefaultPaymentMethodForCurrentClient(pmId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/payments/default-method")
    public ResponseEntity<?> getDefault() {
        try {
            return ResponseEntity.ok(paymentService.getDefaultPaymentMethodForCurrentClient());
        }catch (StripeException se) {
            throw new HEADBusinessException("Error al obtener la tarjeta predeterminada: " + se.getMessage());
        }
    }

    @PostMapping("/jobs/intent")
    public ResponseEntity<?> createIntent(
            @RequestBody HEADPaymentStripeAmountRequest req
    ) {
        var resp = paymentService.createPaymentIntentionForJob(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/paymentMethods")
    public ResponseEntity<?> listMethods() {
        try {
            var methods = paymentService.listPaymentMethodsForCurrentClient();
            return ResponseEntity.ok(methods);
        } catch (StripeException e) {
            throw new HEADBusinessException("Error al listar tarjetas: " + e.getMessage());
        }
    }

    @PostMapping("/jobs/{jobId}/capture")
    public ResponseEntity<?> capture(@PathVariable Long jobId) throws StripeException {
        paymentService.capturePaymentForJob(jobId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable String paymentMethodId) throws StripeException {
        try {
            paymentService.detachPaymentMethodForCurrentClient(paymentMethodId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (StripeException e) {
            throw new HEADBusinessException("Error al eliminar la tarjeta: " + e.getMessage());
        }
    }
}

