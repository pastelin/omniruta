package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFilters;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.claims.HEADClaims;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.HEADServiceAuthentications;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADPersonalService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HEADJwtRequestFilter extends OncePerRequestFilter {

    private final HEADJwtGenerator jwt;
    private final HEADServiceAuthentications authLoader;
    private final HEADSecurityProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String h = req.getHeader(HEADClaims.AUTHORIZATION);
        if (h == null || !h.startsWith(HEADClaims.BEARER)) {
            chain.doFilter(req, res);
            return;
        }

        String token = h.substring(HEADClaims.BEARER.length());
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .setAllowedClockSkewSeconds(props.getJwt().getClockSkewSeconds())
                    .requireIssuer(props.getJwt().getIssuer())
                    .requireAudience(props.getJwt().getAudience())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                chain.doFilter(req, res);
                return;
            }

            var authorities = extractAuthorities(claims);
            if (authorities.isEmpty()) {
                // fallback: roles desde BD si no vinieron en el JWT
                var ud = authLoader.loadUserOrClientByUsername(username);
                if (jwt.validateToken(token, ud)) {
                    var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (JwtException | HEADBadRequestException e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(req, res);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractAuthorities(Claims c) {
        Object raw = c.get(HEADClaims.ROLES);
        List<String> roles = (raw instanceof Collection<?> col)
                ? col.stream().map(Object::toString).toList()
                : (raw instanceof String s)
                ? Arrays.stream(s.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList()
                : List.of();
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
