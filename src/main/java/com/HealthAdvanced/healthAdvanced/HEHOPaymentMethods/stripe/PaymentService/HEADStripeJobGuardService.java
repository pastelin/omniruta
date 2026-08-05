package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelledBy;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HEADStripeJobGuardService {

    private final HEADJobRepository jobRepo;

    @Transactional
    public void assertJobNotCanceledInStripe(HEADJob job) {
        if (job == null) throw new HEADBadRequestException("Servicio no encontrado");

        String piId = job.getPaymentIntentId();
        if (piId == null || piId.isBlank()) return; // no hay pago aún

        try {
            PaymentIntent pi = PaymentIntent.retrieve(piId);
            String status = pi.getStatus(); // canceled / requires_capture / succeeded ...

            job.setStripeStatusRaw(status);

            if ("canceled".equals(status)) {
                job.setPaymentStatus(HEADPaymentStatus.CANCELED);
                job.setState(HEADJobState.CANCELLED);
                job.setCancelledAt(Instant.now());
                job.setCancelledBy(HEADCancelledBy.SYSTEM);
                jobRepo.save(job);

                throw new HEADBadRequestException(
                        "Este servicio ya venció o fue cancelado. Crea una nueva solicitud."
                );
            }

            // opcional: si requiere_capture => AUTHORIZED
            if ("requires_capture".equals(status) && job.getPaymentStatus() != HEADPaymentStatus.AUTHORIZED) {
                job.setPaymentStatus(HEADPaymentStatus.AUTHORIZED);
                jobRepo.save(job);
            }

        } catch (StripeException e) {
            throw new HEADBadRequestException("No se pudo validar el pago. Intenta de nuevo.");
        }
    }
}

