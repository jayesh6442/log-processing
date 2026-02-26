package com.jayesh.ingestionservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public AuthenticatedUser parseUser(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        if (expiration == null || expiration.before(new Date())) {
            throw new IllegalArgumentException("Token expired");
        }

        Long userId = convertToLong(claims.get("userId"));
        String role = String.valueOf(claims.get("role"));
        String email = claims.getSubject();
        return new AuthenticatedUser(userId, email, role);
    }

    private Long convertToLong(Object value) {
        if (value instanceof Integer number) {
            return number.longValue();
        }
        if (value instanceof Long number) {
            return number;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
