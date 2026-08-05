package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken;

public record HEADTokenModel(
       String tokenAccess,
       Long expiresAt
) {
}
