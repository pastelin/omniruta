package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.handler;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.dto.HEADApiError;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters.HEADCorrelationIdFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.helpers.HEADFolioUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String folio = HEADFolioUtils.getFolio(request);
        response.setHeader(HEADCorrelationIdFilter.HEADER_NAME, folio);

        log.warn("401 Unauthorized folio={} path={} reason={}",
                folio, request.getRequestURI(), authException.getMessage());

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "No autenticado o token inválido",
                request.getRequestURI(),
                folio,
                List.of()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADCorrelationIdFilter.HEADER_NAME, folio);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}