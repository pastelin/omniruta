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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADAccessDeniedJsonHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String folio = HEADFolioUtils.getFolio(request);

        log.warn("403 Forbidden folio={} path={} reason={}",
                folio, request.getRequestURI(), accessDeniedException.getMessage());

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "No tienes permisos para acceder a este recurso",
                request.getRequestURI(),
                folio,
                List.of()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADCorrelationIdFilter.HEADER_NAME, folio);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}