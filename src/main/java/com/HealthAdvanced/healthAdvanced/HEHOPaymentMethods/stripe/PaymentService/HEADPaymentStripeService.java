package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.service.HEADShowStaffsToClientsService;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobCreationService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelledBy;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.adjustment.HEADCalculateJobFinancialService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.adjustment.HEADSyncStripeProcessorFeeService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.Entity.Dto.HEADCardSummaryDto;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentMethodRequest;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentStripeAmountRequest;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response.HEADDefaultPaymentMethodDto;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response.HEADDefaultPaymentMethodResponse;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response.HEADPaymentStripeResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClientToCustomer;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.repositories.IHEADClientToCustomerRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodListParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Slf4j
@Service
@RequiredArgsConstructor
public class HEADPaymentStripeService {

    private final HEADJwtGenerator jwt;
    private final HEADClientsRepository headClientsRepository;
    private final IHEADClientToCustomerRepository clientToCustomerRepo;
    private final HEADJobRepository jobRepo;
    private final HEADJobCreationService jobCreationService;
    private final HEADCalculateJobFinancialService headCalculateJobFinancialService;
    private final HEADSyncStripeProcessorFeeService headSyncStripeProcessorFeeService;


    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    // -----------------------------
    // Helper: obtener cliente actual
    // -----------------------------
    private HEADClients getCurrentClient() {
        String uuid = jwt.getUserNamePersonalUser();
        return headClientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));
    }

    // --------------------------------------
    // Helper: obtener/crear Customer Stripe
    // --------------------------------------
    private HEADClientToCustomer getOrCreateCustomerForCurrentClient() {
        HEADClients client = getCurrentClient();

        return clientToCustomerRepo.findByIdClient(client)
                .orElseGet(() -> {
                    try {
                        Map<String, Object> customerParams = new HashMap<>();
                        customerParams.put("email", client.getEmail());
                        // puedes agregar nombre, teléfono, etc
                        Customer customer = Customer.create(customerParams);

                        HEADClientToCustomer c2c = new HEADClientToCustomer();
                        c2c.setIdClient(client);
                        c2c.setCustomerId(customer.getId());
                        // si tienes campo defaultPaymentMethodId, inicialízalo en null
                        return clientToCustomerRepo.save(c2c);
                    } catch (StripeException e) {
                        throw new HEADBusinessException("No se pudo crear el cliente en Stripe: " + e.getMessage());
                    }
                });
    }

    // -------------------------
    // 1) Adjuntar PaymentMethod
    // -------------------------
    @Transactional
    public void attachPaymentMethod(HEADPaymentMethodRequest req) {
        if (req.getPaymentMethodId() == null || req.getPaymentMethodId().isBlank()) {
            throw new HEADBadRequestException("paymentMethodId es requerido");
        }

        HEADClientToCustomer c2c = getOrCreateCustomerForCurrentClient();

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(req.getPaymentMethodId());

            Map<String, Object> params = new HashMap<>();
            params.put("customer", c2c.getCustomerId());
            paymentMethod.attach(params);

            // opcional: guardar como default en stripe y en tu tabla
            if (Boolean.TRUE.equals(req.getSetAsDefault())) {
                Customer stripeCustomer = Customer.retrieve(c2c.getCustomerId());
                CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                        .setInvoiceSettings(
                                CustomerUpdateParams.InvoiceSettings.builder()
                                        .setDefaultPaymentMethod(paymentMethod.getId())
                                        .build()
                        )
                        .build();
                stripeCustomer.update(updateParams);

                c2c.setDefaultPaymentMethodId(paymentMethod.getId());
                clientToCustomerRepo.save(c2c);
            }

        } catch (StripeException e) {
            throw new HEADBusinessException("Error al vincular la tarjeta con Stripe: " + e.getMessage());
        }
    }

    public List<HEADCardSummaryDto> listPaymentMethodsForCurrentClient() throws StripeException {
        HEADClientToCustomer customer = getOrCreateCustomerForCurrentClient();
        if (customer == null) {
            return List.of();
        }

        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customer.getCustomerId())
                .setType(PaymentMethodListParams.Type.CARD)
                .setLimit(100L)
                .build();

        PaymentMethodCollection collection = PaymentMethod.list(params);

        return collection.getData().stream().filter(pms -> pms.getCard() != null).map(pm -> {
            HEADCardSummaryDto dto = new HEADCardSummaryDto();
            dto.setId(pm.getId());
            PaymentMethod.Card card = pm.getCard();
            dto.setBrand(card.getBrand());   // "visa", "mastercard", ...
            dto.setLast4(card.getLast4());
            dto.setExpMonth(card.getExpMonth());
            dto.setExpYear(card.getExpYear());
            var cardDefaults = clientToCustomerRepo.
                    findAll().stream().
                    filter(cardDefault -> Objects.equals(cardDefault.getDefaultPaymentMethodId(), pm.getId()))
                    .toList()
                    .isEmpty();
            dto.setDefault(!cardDefaults);
            return dto;
        }).toList();
    }

    public int countListPaymentsMethods() throws StripeException {
        HEADClientToCustomer customer = getOrCreateCustomerForCurrentClient();
        if (customer == null) {
            return 0;
        }

        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customer.getCustomerId())
                .setType(PaymentMethodListParams.Type.CARD)
                .setLimit(100L)
                .build();

        PaymentMethodCollection collection = PaymentMethod.list(params);
        return collection.getData().size();
    }

    @Transactional
    public HEADPaymentStripeResponse createPaymentIntentionForJob(
            HEADPaymentStripeAmountRequest req
    ) {
        if (req.getPaymentMethodId() == null || req.getPaymentMethodId().isBlank()) {
            throw new HEADBadRequestException("paymentMethodId es requerido");
        }

        HEADClients currentClient = getCurrentClient();

        // 1) Construimos ServiceRequest + Job EN MEMORIA
        HEADJob job = jobCreationService.createAndSaveNewJob(req, currentClient.getUuIdUser());


        // seguridad: el job debe ser del cliente logueado
        if (!job.getClient().getIdUser().equals(currentClient.getIdUser())) {
            throw new HEADBadRequestException("No tienes permisos sobre este servicio");
        }

        HEADClientToCustomer c2c = getOrCreateCustomerForCurrentClient();

        try {
            double amountMx = job.getAmount() != null ? job.getAmount().doubleValue() : 0.0;
            if (amountMx <= 0) {
                throw new HEADBadRequestException("Monto inválido para el pago");
            }

            long amountInCents = (long) (amountMx * 100L);

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInCents)
                            .setCurrency("mxn")
                            .setCustomer(c2c.getCustomerId())
                            .setPaymentMethod(req.getPaymentMethodId())
                            .addPaymentMethodType("card")
                            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                            .setConfirm(true) // autoriza de una vez
                            .setDescription("Job tmp - " +
                                    (job.getRequest() != null && job.getRequest().getPkg() != null
                                            ? job.getRequest().getPkg().getTitle()
                                            : "Servicio a domicilio"))
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);


            job.setPaymentIntentId(intent.getId());
            job.setStripeStatusRaw(intent.getStatus());
            job.setPaymentStatus(HEADPaymentStatus.fromStripeStatus(intent.getStatus()));

            job.setState(HEADJobState.PENDING_ASSIGNMENT);

            // 2) AHORA sí persistimos el Job porque Stripe ya dio ok
            HEADJob jobCurrent = jobRepo.save(job);

            HEADPaymentStripeResponse resp = new HEADPaymentStripeResponse();
            resp.setJobId(jobCurrent.getId());
            resp.setClientSecret(intent.getClientSecret());
            resp.setPaymentIntentId(intent.getId());
            resp.setStripeStatus(intent.getStatus());
            resp.setPaymentStatus(jobCurrent.getPaymentStatus());

            return resp;

        } catch (StripeException e) {
            // aquí podrías LOGUEAR y opcionalmente marcar el ServiceRequest como fallido
            throw new HEADBusinessException("Error al procesar el pago con Stripe: " + e.getMessage());
        }
    }


    // --------------------------------
    // 3) Capturar PaymentIntent de Job
    // --------------------------------
    @Transactional
    public void capturePaymentForJob(Long jobId) {
        try {
            HEADJob job = jobRepo.findById(jobId)
                    .orElseThrow(() -> new HEADBadRequestException("Job no encontrado: " + jobId));


            if (job.getPaymentIntentId() == null || job.getPaymentIntentId().isBlank()) {
                throw new HEADBadRequestException("El Job no tiene PaymentIntentId configurado");
            }

            if (job.getPaymentStatus() == HEADPaymentStatus.CAPTURED) return;

            PaymentIntent intent = PaymentIntent.retrieve(job.getPaymentIntentId());
            String status = intent.getStatus();

            if ("canceled".equals(status)) {
                job.setStripeStatusRaw(status);
                job.setPaymentStatus(HEADPaymentStatus.CANCELED);
                jobRepo.save(job);
                throw new HEADBadRequestException("El pago venció o fue cancelado. El cliente debe reintentar el pago.");
            }

            if ("succeeded".equals(status)) {
                job.setStripeStatusRaw(status);
                job.setPaymentStatus(HEADPaymentStatus.CAPTURED);
                job.setCapturedAt(Instant.now());
                String chargeId = intent.getLatestCharge();
                if (chargeId != null) job.setPaymentId(chargeId);
                jobRepo.save(job);
                return;
            }

            if (!"requires_capture".equals(status)) {
                job.setStripeStatusRaw(status);
                job.setPaymentStatus(HEADPaymentStatus.fromStripeStatus(status));
                jobRepo.save(job);
                throw new HEADBadRequestException("No se pudo capturar el pago. Status=" + status);
            }

            // Si Stripe ya está listo, tu BD debe reflejar AUTHORIZED
            if (job.getPaymentStatus() != HEADPaymentStatus.AUTHORIZED) {
                job.setPaymentStatus(HEADPaymentStatus.AUTHORIZED);
            }

            PaymentIntent captured = intent.capture(PaymentIntentCaptureParams.builder().build());
            String capturedStatus = captured.getStatus();

            job.setStripeStatusRaw(capturedStatus);
            job.setPaymentStatus(HEADPaymentStatus.fromStripeStatus(capturedStatus));
            job.setCapturedAt(Instant.now());

            String capturedChargeId = captured.getLatestCharge();
            if (capturedChargeId != null) job.setPaymentId(capturedChargeId);

            jobRepo.save(job);

            if (job.getState() == HEADJobState.COMPLETED
                    && job.getPaymentStatus() == HEADPaymentStatus.CAPTURED) {

                headCalculateJobFinancialService.createSnapshotForCompletedJob(
                        job.getId(),
                        HEADPaymentProcessor.STRIPE
                );

                headSyncStripeProcessorFeeService.trySyncRealStripeFee(job.getId(), job.getPaymentIntentId(), job.getPaymentId());
            }

        } catch (StripeException e) {
            throw new HEADBusinessException("No se pudo capturar el pago en Stripe: " + e.getMessage());
        }
    }




    // -------------------------
    // (Opcional) set default pm
    // -------------------------
    @Transactional
    public void setDefaultPaymentMethodForCurrentClient(String paymentMethodId) throws StripeException {
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new HEADBadRequestException("paymentMethodId es requerido");
        }

        String uuid = jwt.getUserNamePersonalUser();

        HEADClients client = headClientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        HEADClientToCustomer rel = clientToCustomerRepo.findByIdClient(client)
                .orElseThrow(() -> new HEADBadRequestException("El cliente no tiene Customer en Stripe"));

        String customerId = rel.getCustomerId();
        if (customerId == null || customerId.isBlank()) {
            throw new HEADBadRequestException("El cliente no tiene customerId en Stripe");
        }

        // Validar que el PaymentMethod pertenezca a este customer
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
        if (pm == null || pm.getCustomer() == null || !customerId.equals(pm.getCustomer())) {
            throw new HEADBadRequestException("La tarjeta no pertenece al cliente actual");
        }

        // Actualizar invoice_settings.default_payment_method en Stripe
        Customer customer = Customer.retrieve(customerId);

        Map<String, Object> invoiceSettings = new HashMap<>();
        invoiceSettings.put("default_payment_method", paymentMethodId);

        Map<String, Object> params = new HashMap<>();
        params.put("invoice_settings", invoiceSettings);

        customer.update(params);

        // Guardar en BD
        rel.setDefaultPaymentMethodId(paymentMethodId);
        clientToCustomerRepo.save(rel);
    }

    public HEADDefaultPaymentMethodResponse getDefaultPaymentMethodForCurrentClient() throws StripeException {
        String uuid = jwt.getUserNamePersonalUser();

        HEADClients client = headClientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        HEADClientToCustomer rel = clientToCustomerRepo.findByIdClient(client)
                .orElseThrow(() -> new HEADBadRequestException("El cliente no tiene Customer en Stripe"));

        String customerId = rel.getCustomerId();
        if (customerId == null || customerId.isBlank()) {
            throw new HEADBadRequestException("El cliente no tiene customerId en Stripe");
        }

        // 1) Intentar usar el default guardado
        String defaultPmId = rel.getDefaultPaymentMethodId();
        PaymentMethod pm = null;

        if (defaultPmId != null && !defaultPmId.isBlank()) {
            pm = PaymentMethod.retrieve(defaultPmId);

            // Opcional: validar que aún sea del mismo customer
            if (pm == null || pm.getCustomer() == null || !customerId.equals(pm.getCustomer())) {
                pm = null;
            }
        }

        // 2) Si no hay default o es inválido → tomar la primera tarjeta del customer
        if (pm == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("customer", customerId);
            params.put("type", "card");

            PaymentMethodCollection list = PaymentMethod.list(params);
            if (list.getData().isEmpty()) {
                // No tiene tarjetas
                return new HEADDefaultPaymentMethodResponse(false,false,null);
            }

            pm = list.getData().stream().findFirst().orElse(null);
            rel.setDefaultPaymentMethodId(pm != null ? pm.getId() : null);
            clientToCustomerRepo.save(rel);
        }

        PaymentMethod.Card card = pm != null ? pm.getCard() : null;
        String brand = card != null && card.getBrand() != null ? card.getBrand() : "unknown";
        String last4 = card != null && card.getLast4() != null ? card.getLast4() : "0000";

        var defaultCard = new HEADDefaultPaymentMethodDto(
                pm != null ? pm.getId() : null,
                brand,          // "visa", "mastercard", "amex", etc
                last4,
                true
        );
        return new HEADDefaultPaymentMethodResponse(
                true,
                true,
                defaultCard
        );
    }

    @Transactional
    public void detachPaymentMethodForCurrentClient(String paymentMethodId) throws StripeException {
        String uuid = jwt.getUserNamePersonalUser();

        HEADClients client = headClientsRepository.findByUuIdUser(uuid)
                .orElseThrow(() -> new HEADBadRequestException("Cliente no encontrado"));

        HEADClientToCustomer clientToCustomer = clientToCustomerRepo.findByIdClient(client)
                .orElseThrow(() -> new HEADBadRequestException("El cliente no tiene Customer en Stripe"));
        if (clientToCustomer == null) {
            throw new HEADBadRequestException("Cliente no tiene customerId en Stripe");
        }

        // 2) Obtener el PaymentMethod de Stripe
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);

        // 3) Validar que pertenece a este customer
        if (pm.getCustomer() == null ||
                !pm.getCustomer().equals(clientToCustomer.getCustomerId())) {
            throw new HEADBadRequestException("La tarjeta no pertenece a este cliente");
        }


        pm.detach();

        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(clientToCustomer.getCustomerId())
                .setType(PaymentMethodListParams.Type.CARD)
                .build();

        PaymentMethodCollection collection = PaymentMethod.list(params);
        var cardFirst = collection.getData().stream().filter(pms -> pms.getCard() != null).findFirst().orElse(null);
        setDefaultPaymentMethodForCurrentClient(cardFirst != null ? cardFirst.getId() : null);
    }

    @Transactional
    public void cancelPaymentForJob(HEADJob job, HEADCancelReason reason, HEADClients currentClient) {

        if (!job.getClient().getIdUser().equals(currentClient.getIdUser())) {
            throw new HEADBadRequestException("No tienes permisos sobre este servicio");
        }

        if (job.getPaymentIntentId() == null || job.getPaymentIntentId().isBlank()) {
            // si no hay PI, solo marcamos cancelado en BD
            job.setPaymentStatus(HEADPaymentStatus.NONE);
            jobRepo.save(job);
            return;
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(job.getPaymentIntentId());
            String statusCurrentPayment = intent.getStatus();
            HEADPaymentStatus status = HEADPaymentStatus.fromStripeStatus(statusCurrentPayment);

            // Idempotencia: si ya está cancelado, no hacemos nada en Stripe
            if (HEADPaymentStatus.CANCELED.equals(status)) {
                job.setStripeStatusRaw(statusCurrentPayment);
                job.setPaymentStatus(HEADPaymentStatus.CANCELED);
                job.setCancelReason(reason);
                job.setCancelledAt(Instant.now());
                jobRepo.save(job);
                return;
            }

            // Si ya fue capturado (succeeded) no se puede "cancelar": hay que reembolsar
            if (HEADPaymentStatus.CAPTURED.equals(status)) {
                throw new HEADBadRequestException("El pago ya fue capturado; usa reembolso.");
            }

            // Estados típicos para cancelar autorización:
            // requires_capture (autorizado), requires_payment_method, requires_confirmation, requires_action, processing
            intent.cancel();

            PaymentIntent canceled = PaymentIntent.retrieve(job.getPaymentIntentId());

            job.setStripeStatusRaw(canceled.getStatus()); // canceled
            job.setPaymentStatus(HEADPaymentStatus.CANCELED);
            job.setCancelReason(reason);
            job.setCancelledAt(Instant.now());
            jobRepo.save(job);

        } catch (StripeException e) {
            throw new HEADBusinessException("No se pudo cancelar el pago en Stripe: " + e.getMessage());
        }
    }

    @Transactional
    public void assertJobNotCanceledInStripe(HEADJob job) {
        if (job == null) throw new HEADBadRequestException("Servicio no encontrado");

        // Bloquea jobs cerrados (reintento no válido)
        if (job.getState() == HEADJobState.COMPLETED
                || job.getState() == HEADJobState.CANCELLED
                || job.getState() == HEADJobState.EXPIRED
                || job.getState() == HEADJobState.REJECTED) {
            throw new HEADBadRequestException("Este servicio ya no puede solicitarse de nuevo.");
        }

        String piId = job.getPaymentIntentId();
        if (piId == null || piId.isBlank()) return;

        // Si ya terminó el pago, no reasignes
        if (job.getPaymentStatus() == HEADPaymentStatus.CAPTURED
                || job.getPaymentStatus() == HEADPaymentStatus.REFUNDED) {
            throw new HEADBadRequestException("Este servicio ya no puede reasignarse.");
        }

        try {
            PaymentIntent pi = PaymentIntent.retrieve(piId);
            String raw = pi.getStatus();
            HEADPaymentStatus stripeStatus = HEADPaymentStatus.fromStripeStatus(raw);

            job.setStripeStatusRaw(raw);

            if (stripeStatus == HEADPaymentStatus.CANCELED) {
                job.setPaymentStatus(HEADPaymentStatus.CANCELED);
                job.setState(HEADJobState.CANCELLED);
                job.setCancelledAt(Instant.now());
                job.setCancelledBy(HEADCancelledBy.SYSTEM);
                jobRepo.save(job);

                throw new HEADBadRequestException(
                        "Este servicio ya venció o fue cancelado. Crea una nueva solicitud."
                );
            }

            if (stripeStatus == HEADPaymentStatus.AUTHORIZED
                    && job.getPaymentStatus() != HEADPaymentStatus.AUTHORIZED) {
                job.setPaymentStatus(HEADPaymentStatus.AUTHORIZED);
                jobRepo.save(job);
            }

        } catch (StripeException e) {
            throw new HEADBusinessException("No se pudo validar el pago. Intenta de nuevo. " + e.getMessage());
        }
    }


    @Transactional
    public void cancelPaymentForJobBySystem(HEADJob job, HEADCancelReason reason) {

        if (job == null) {
            throw new HEADBadRequestException("Servicio no encontrado");
        }

        if (job.getPaymentIntentId() == null || job.getPaymentIntentId().isBlank()) {
            job.setPaymentStatus(HEADPaymentStatus.NONE);
            job.setCancelReason(reason);
            job.setCancelledAt(Instant.now());
            jobRepo.save(job);
            return;
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(job.getPaymentIntentId());
            String stripeStatus = intent.getStatus();
            HEADPaymentStatus currentStatus = HEADPaymentStatus.fromStripeStatus(stripeStatus);

            job.setStripeStatusRaw(stripeStatus);

            if (HEADPaymentStatus.CANCELED.equals(currentStatus)) {
                job.setPaymentStatus(HEADPaymentStatus.CANCELED);
                job.setCancelReason(reason);
                job.setCancelledAt(Instant.now());
                jobRepo.save(job);
                return;
            }

            // Si ya fue capturado, aquí necesitarías refund, no cancel
            if (HEADPaymentStatus.CAPTURED.equals(currentStatus)) {
                throw new HEADBadRequestException("El pago ya fue capturado; debes reembolsarlo.");
            }

            intent.cancel();

            PaymentIntent canceled = PaymentIntent.retrieve(job.getPaymentIntentId());

            job.setStripeStatusRaw(canceled.getStatus());
            job.setPaymentStatus(HEADPaymentStatus.CANCELED);
            job.setCancelReason(reason);
            job.setCancelledAt(Instant.now());
            jobRepo.save(job);

        } catch (StripeException e) {
            throw new HEADBusinessException("No se pudo cancelar el pago en Stripe: " + e.getMessage());
        }
    }
}
