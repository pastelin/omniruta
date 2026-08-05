package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.enums.HEADTypeUser;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADConstantsSecurity;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.claims.HEADClaims;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties.HEADSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HEADJwtGenerator {


    private final HEADSecurityProperties props;

    private SecretKey key() {
        // Si el secret está en Base64: Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getJwt().getSecret()))
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getJwt().getSecret()));

    }


    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public Claims extractAllClaims(String token) {
        var jwtCfg = props.getJwt();
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .setAllowedClockSkewSeconds(jwtCfg.getClockSkewSeconds())
                .requireIssuer(jwtCfg.getIssuer())
                .requireAudience(jwtCfg.getAudience())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static String[] splitRoles(String rolesCsv) {
        return Arrays.stream(Objects.toString(rolesCsv, "").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public HEADTokenModel generateToken(UserDetails userDetails, HEADTypeUser typeUser) {
        var jwtCfg = props.getJwt();
        var ttlSeconds = typeUser == HEADTypeUser.ADMIN ? jwtCfg.getTtlSecondsAdmin() : jwtCfg.getTtlSeconds();
        Date exp = new Date(System.currentTimeMillis() + ttlSeconds * 1000L);

        String token = Jwts.builder()
                .setIssuer(jwtCfg.getIssuer())
                .setAudience(jwtCfg.getAudience())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(exp)
                .claim(HEADClaims.ROLES, userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                        .toList())
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        return new HEADTokenModel(token, exp.getTime());
    }
    public boolean validateToken(String token, UserDetails user) {
        var c = extractAllClaims(token);
        return user.getUsername().equals(c.getSubject()) && c.getExpiration().after(new Date());
    }

    private Date expirationToken() {
        Calendar calendar = Calendar.getInstance();
        int addedMonths = 12;
        calendar.add(Calendar.MONTH, addedMonths);
        return calendar.getTime();
    }

    public String getUserNamePersonalUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    if (auth instanceof JwtAuthenticationToken jat) return jat.getToken().getSubject();
                    return auth.getName();
                })
                .orElse(null);
    }
}
