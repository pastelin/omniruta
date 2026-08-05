package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;

import java.time.Instant;
import java.util.List;

public record HEADGetUserProfileResponse(
        boolean success,
        Data data
) {

    public record Data(
            List<PaymentMethod> paymentMethods,
            MedicalRecords medicalRecords,
            Prescriptions prescriptions,
            Notifications notifications,
            Preferences preferences,
            Subscription subscription,
            Insurance insurance,
            Favorites favorites,
            LoyaltyPoints loyaltyPoints,
            List<FamilyMember> familyMembers,
            Security security,
            Settings settings
    ) {}

    public record PaymentMethod(
            String id,
            String type,
            String brand,
            String last4,
            String expiryMonth,
            String expiryYear,
            String holderName,
            boolean isDefault
    ) {}

    public record MedicalRecords(
            int totalRecords,
            String lastUpdate,
            boolean hasAllergies,
            boolean hasChronicConditions
    ) {}

    public record Prescriptions(
            int active,
            int expired,
            String lastPrescription
    ) {}

    public record Notifications(
            int unreadCount,
            boolean enabled,
            NotificationCategories categories
    ) {}

    public record NotificationCategories(
            boolean appointments,
            boolean promotions,
            boolean reminders,
            boolean results
    ) {}

    public record Preferences(
            String language,
            String theme,
            boolean emailNotifications,
            boolean pushNotifications,
            boolean smsNotifications,
            String reminderTime
    ) {}

    public record Subscription(
            boolean isActive,
            String plan,
            String startDate,
            String expiresAt,
            boolean autoRenew,
            List<String> benefits
    ) {}

    public record Insurance(
            boolean hasInsurance,
            String provider,
            String policyNumber,
            String coverageType,
            String expiryDate
    ) {}

    public record Favorites(
            int doctors,
            int services,
            int locations
    ) {}

    public record LoyaltyPoints(
            int current,
            int totalEarned,
            int totalSpent,
            String tier,
            String nextTier,
            int pointsToNextTier
    ) {}

    public record FamilyMember(
            String id,
            String name,
            String relationship,
            String dateOfBirth,
            String bloodType
    ) {}

    public record Security(
            boolean twoFactorEnabled,
            boolean biometricEnabled,
            String lastPasswordChange,
            int activeSessions
    ) {}

    public record Settings(
            boolean autoSaveRecords,
            boolean shareDataWithDoctors,
            boolean allowMarketingEmails,
            String defaultLocation
    ) {}
}