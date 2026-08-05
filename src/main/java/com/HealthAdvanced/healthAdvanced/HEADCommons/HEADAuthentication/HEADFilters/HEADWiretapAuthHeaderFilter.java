package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class HEADWiretapAuthHeaderFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HEADWiretapAuthHeaderFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        log.info(">> {} {} Auth={} Platform={} Device-Id={}, App-Version={}, App-Build={}",
                req.getMethod(), req.getRequestURI(),
                req.getHeader("Authorization"),
                req.getHeader(HEADHeadersConstants.PLATFORM),
                req.getHeader(HEADHeadersConstants.DEVICE_ID),
                req.getHeader(HEADHeadersConstants.APP_VERSION),
                req.getHeader(HEADHeadersConstants.APP_BUILD));
        chain.doFilter(req, res);
    }
}
