package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class HEADWiretapAuthHeaderFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HEADWiretapAuthHeaderFilter.class);
    private static final int MAX_LOG_BODY_BYTES = 8_192;
    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(?i)(\\\"(?:password|accessToken|refreshToken|token|fcmToken)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        log.info(">> {} {} authHeaderPresent={} Platform={} Device-Id={}, App-Version={}, App-Build={}",
                req.getMethod(), req.getRequestURI(),
            req.getHeader("Authorization") != null,
                req.getHeader(HEADHeadersConstants.PLATFORM),
                req.getHeader(HEADHeadersConstants.DEVICE_ID),
                req.getHeader(HEADHeadersConstants.APP_VERSION),
                req.getHeader(HEADHeadersConstants.APP_BUILD));

        var request = new ContentCachingRequestWrapper(req, MAX_LOG_BODY_BYTES);
        var response = new ContentCachingResponseWrapper(res);
        try {
            chain.doFilter(request, response);
        } finally {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("<< {} {} status={} principal={} authorities={}",
                    req.getMethod(), req.getRequestURI(), response.getStatus(),
                    auth != null ? auth.getName() : "anonymous",
                    auth != null ? auth.getAuthorities() : "[]");
            log.info("[HTTP_PAYLOAD] {} {} request={} response={}",
                    req.getMethod(), req.getRequestURI(),
                    bodyForLog(request.getContentType(), request.getContentAsByteArray()),
                    bodyForLog(response.getContentType(), response.getContentAsByteArray()));
            response.copyBodyToResponse();
        }
    }

    private String bodyForLog(String contentType, byte[] body) {
        if (body == null || body.length == 0) return "<empty>";
        if (contentType == null || !contentType.toLowerCase().contains("json")) return "<not-json>";

        String content = new String(body, StandardCharsets.UTF_8);
        String redacted = SENSITIVE_JSON_FIELD.matcher(content).replaceAll("$1***$2");
        return body.length >= MAX_LOG_BODY_BYTES ? redacted + "...<truncated>" : redacted;
    }
}
