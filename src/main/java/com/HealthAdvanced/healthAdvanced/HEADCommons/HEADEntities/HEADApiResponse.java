package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public record HEADApiResponse<T> (
        boolean success,
        T result,
        String folio,
        String mensaje,
        int code
) {
    public static <T> HEADApiResponse<T> ok(T result, String mensaje) {
        return new HEADApiResponse<>(
                true,
                result,
                HEADFolioGen.newFolio(),
                mensaje,
                200
        );
    }

    public static <T> HEADApiResponse<T> ok(T result) {
        return ok(result, "OK");
    }

    public static <T> HEADApiResponse<T> fail(int code, String mensaje) {
        return new HEADApiResponse<>(
                false,
                null,
                HEADFolioGen.newFolio(),
                mensaje,
                code
        );
    }
}
