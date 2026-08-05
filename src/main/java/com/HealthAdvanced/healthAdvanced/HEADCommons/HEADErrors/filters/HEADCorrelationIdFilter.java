package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class HEADCorrelationIdFilter extends OncePerRequestFilter {

    public static final String FOLIO_KEY = "folio";
    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String folio = request.getHeader(HEADER_NAME);
        if (folio == null || folio.isBlank()) {
            folio = UUID.randomUUID().toString();
        }

        request.setAttribute(FOLIO_KEY, folio);
        response.setHeader(HEADER_NAME, folio);

        MDC.put(FOLIO_KEY, folio);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(FOLIO_KEY);
        }
    }
}