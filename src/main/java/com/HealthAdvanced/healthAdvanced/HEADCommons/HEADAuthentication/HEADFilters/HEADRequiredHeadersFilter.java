package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.headerConfig.HEADCaseInsensitiveHeadersRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.dto.HEADApiError;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters.HEADCorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADRequiredHeadersFilter extends OncePerRequestFilter {

    private final HEADSecurityProperties props;
    private final AntPathMatcher ant = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!props.getHeaders().isEnforceOnPublic()) {
            String path = request.getServletPath();
            return props.getPaths().getPublicPaths().stream()
                    .anyMatch(pattern -> ant.match(pattern, path));
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        var wrapped = new HEADCaseInsensitiveHeadersRequest(req);
        var required = props.getHeaders().getRequired();
        var aliasPatterns = props.getHeaders().getAliases();
        var presentNames = Collections.list(wrapped.getHeaderNames());

        var missing = required.stream()
                .filter(reqName -> {
                    var pattern = aliasPatterns.getOrDefault(reqName, Pattern.quote(reqName));
                    var compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    return presentNames.stream()
                            .filter(h -> compiled.matcher(h).matches())
                            .map(wrapped::getHeader)
                            .noneMatch(v -> v != null && !v.isBlank());
                })
                .toList();

        if (!missing.isEmpty()) {
            String folio = (String) req.getAttribute(HEADCorrelationIdFilter.FOLIO_KEY);

            HEADApiError body = new HEADApiError(
                    java.time.OffsetDateTime.now().toString(),
                    HttpServletResponse.SC_BAD_REQUEST,
                    "MISSING_HEADERS",
                    "Faltan headers requeridos",
                    req.getRequestURI(),
                    folio,
                    missing
            );

            log.warn("Missing headers folio={} path={} missing={}",
                    folio, req.getRequestURI(), missing);

            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.setHeader(HEADCorrelationIdFilter.HEADER_NAME, folio);

            new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValue(res.getOutputStream(), body);
            return;
        }

        chain.doFilter(wrapped, res);
    }
}