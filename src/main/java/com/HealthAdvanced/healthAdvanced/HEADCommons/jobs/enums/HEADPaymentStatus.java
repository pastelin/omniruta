package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums;



public enum HEADPaymentStatus {
    NONE,        // aún no se ha intentado nada
    PENDING,     // en proceso / requiere algo del usuario
    AUTHORIZED,  // autorizado (hold) pero sin capturar
    CAPTURED,    // cobro exitoso
    REFUNDED,    // reembolso exitoso
    CANCELED,
    FAILED;      // error duro

    public static HEADPaymentStatus fromStripeStatus(String raw) {
        if (raw == null) return NONE;

        return switch (raw) {
            // Todavía no está pagado, falta algo
            case "requires_payment_method",
                 "requires_confirmation",
                 "requires_action",
                 "processing" -> PENDING;

            // PaymentIntent creado con capture_method=manual y ya autorizado
            case "requires_capture" -> AUTHORIZED;

            // Listo, se cobró bien
            case "succeeded" -> CAPTURED;

            // Cancelado antes de capturar (puedes tratarlo como FAILED de negocio)
            case "canceled" -> CANCELED;

            // Cualquier cosa rara
            default -> FAILED;
        };
    }
}
