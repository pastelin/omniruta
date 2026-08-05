package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.service;


import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADAccountHomeResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.mapers.HEADMapAccount;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.proposValues.values.HEADValuesProperties;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffReviewRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService.HEADPaymentStripeService;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class HEADAccountHomeService {

    private final HEADJobRepository jobRepo;
    private final HEADStaffReviewRepository reviewRepo;
    private final HEADClientsRepository clientsRepo;
    private final HEADMapAccount mapAccount;
    private final HEADJwtGenerator headJwtGenerator;
    private final HEADPrescriptionJpaRepository prescriptionRepo;
    private final HEADValuesProperties valuesProperties;
    private final HEADPaymentStripeService stripePaymentService;


    public HEADAccountHomeResponse getHome() {

        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADClients client = clientsRepo.findByUuIdUser(getUUID)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + getUUID));
        long clientId = client.getIdUser();

        var user = mapAccount.mapUserAccount(client);

        long completed = jobRepo.countByClient_IdUserAndState(clientId, HEADJobState.COMPLETED);

        Instant now = Instant.now();

        long upcomingScheduled = jobRepo.countByClient_IdUserAndStateAndScheduledTimeAfter(
                clientId,
                HEADJobState.SCHEDULED,
                now
        );

        // --- SATISFACTION ---
        double satisfaction = reviewRepo.satisfactionAvgForClient(clientId);
        double satisfactionRounded = round1(satisfaction);

        // --- POINTS ---
        int factor = 10;
        long pointsLong = completed * factor;
        int points = (pointsLong > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) pointsLong;

        var stats = new HEADAccountHomeResponse.StatsCards(
                (int) completed,
                satisfactionRounded,
                points
        );

        // --- SERVICE LOCATIONS (historial por startAddress) ---
        long locations = jobRepo.countDistinctStartAddressesByClient(clientId);

        int paymentMethodsCount = 0;
        try {
            paymentMethodsCount = stripePaymentService.countListPaymentsMethods();
        } catch (Exception e) {
            paymentMethodsCount = 0;
        }

        var accountItems = new HEADAccountHomeResponse.AccountItems(
                (int) locations,
                paymentMethodsCount,
                HEADAccountHomeResponse.IdentityStatus.NOT_STARTED
        );

        int VALID_DAYS = valuesProperties.getPrescription().getValidDays();
        Instant from = Instant.now().minus(VALID_DAYS, ChronoUnit.DAYS);

        String clientUuid = client.getUuIdUser();

        long activePrescriptions = (clientUuid == null || clientUuid.isBlank())
                ? 0
                : prescriptionRepo.countActivePrescriptionsLastDays(clientUuid, HEADPrescriptionStatus.ISSUED,  from);

        long activeMedications = (clientUuid == null || clientUuid.isBlank())
                ? 0
                : prescriptionRepo.countActiveMedicationsLastDays(clientUuid, HEADPrescriptionStatus.ISSUED, from);

        var medicalItems = new HEADAccountHomeResponse.MedicalItems(
                (int) upcomingScheduled,
                (int) activePrescriptions,
                (int) activeMedications,
                0,
                0,
                (int) completed
        );

        // --- REWARDS ---
        var rewardsItems = new HEADAccountHomeResponse.RewardsItems(
                points,
                0,      // activeCouponsCount
                "Free", // membershipPlan
                false   // membershipActive
        );

        // --- PREFERENCES ---
        var preferencesItems = new HEADAccountHomeResponse.PreferencesItems(
                true,
                "es-MX",
                "light", // theme
                1
        );

        var data = new HEADAccountHomeResponse.Data(
                user,
                stats,
                accountItems,
                medicalItems,
                rewardsItems,
                preferencesItems
        );

        return new HEADAccountHomeResponse(true, data);
    }


    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}