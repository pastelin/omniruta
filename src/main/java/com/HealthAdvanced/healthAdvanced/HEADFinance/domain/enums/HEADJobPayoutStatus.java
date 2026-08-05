package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums;

public enum HEADJobPayoutStatus {
    PENDING,      // todavía no elegible
    ON_HOLD,      // retenido por política
    AVAILABLE,    // ya se puede retirar
    RESERVED,     // ya quedó dentro de un payout
    PAID,         // ya fue pagado
    BLOCKED,      // bloqueado por disputa/revisión
    REVERSED      // revertido por refund/chargeback
}
