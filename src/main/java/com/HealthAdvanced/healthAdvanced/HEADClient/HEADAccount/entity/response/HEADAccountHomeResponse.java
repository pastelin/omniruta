package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response;

public record HEADAccountHomeResponse(
        boolean success,
        Data data
) {
    public record Data(
            User user,
            StatsCards stats,
            AccountItems accountItems,
            MedicalItems medicalItems,
            RewardsItems rewardsItems,
            PreferencesItems preferencesItems
    ) {}

    public record User(
            String fullName,
            String email,
            String phone,
            String profileImage,
            String memberSince,
            String accountType
    ) {}

    // 3 cards arriba: Citas / Valoración / Puntos
    public record StatsCards(
            int appointmentsCount,        // ej 24 (tú decides si total o completed)
            double satisfactionRating,    // ej 4.9 (promedio de reviews del cliente)
            int points                    // ej 1240
    ) {}

    public record AccountItems(
            int serviceLocationsCount,    // basado en startAddress (únicas)
            int paymentMethodsCount,
            IdentityStatus identityStatus
    ) {}

    // Historial médico (subtítulos)
    public record MedicalItems(
            int upcomingAppointmentsCount,
            int activePrescriptionsCount,
            int activeMedTrackingCount,
            int labResultsCount,
            int favoritesCount,
            int completedServicesCount
    ) {}

    public record RewardsItems(
            int pointsAvailable,
            int activeCouponsCount,
            String membershipPlan,
            boolean membershipActive
    ) {}

    public record PreferencesItems(
            boolean notificationsEnabled,
            String languageRegion, // "es-MX"
            String theme,          // "light"
            int linkedDevicesCount
    ) {}

    public enum IdentityStatus { NOT_STARTED, PENDING, VERIFIED }
}