package com.ieum.ansimdonghaeng.common.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    void initialize() {
        validateSecret(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(username, authorities, jwtProperties.getAccessTokenExpirationMinutes(), "access");
    }

    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(username, authorities, jwtProperties.getRefreshTokenExpirationMinutes(), "refresh");
    }

    private String generateToken(String username,
                                 Collection<? extends GrantedAuthority> authorities,
                                 long expirationMinutes,
                                 String type) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((left, right) -> left + " " + right)
                .orElse("");

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .claim("type", type)
                .claim("scope", scope)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getTokenType(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public Authentication createAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(request.getRemoteAddr());
        return authentication;
    }

    public String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(jwtProperties.getHeader());
        String expectedPrefix = jwtProperties.getPrefix() + " ";
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(expectedPrefix)) {
            return authorizationHeader.substring(expectedPrefix.length());
        }
        return null;
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationMinutes() * 60;
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpirationMinutes() * 60;
    }

    private void validateSecret(String secret) {
        if (!StringUtils.hasText(secret) || secret.length() < 32 || isPlaceholder(secret)) {
            throw new IllegalStateException("JWT secret must be configured with a secure value of at least 32 characters.");
        }
    }

    private boolean isPlaceholder(String secret) {
        String normalizedSecret = secret.toLowerCase();
        return normalizedSecret.contains("change-me")
                || normalizedSecret.contains("replace-me")
                || normalizedSecret.contains("your-secret");
    }
}
