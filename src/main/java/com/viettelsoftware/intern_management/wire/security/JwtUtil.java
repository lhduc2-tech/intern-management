package com.viettelsoftware.intern_management.wire.security;

import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.port.secondary.JwtProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil implements JwtProviderPort {

    // Using a securely generated key for HS256 algorithm (min 256 bits)
    // In production, this should be injected via environment variables
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs; // Default 24 hours

    @Override
    public String generateToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDTO.getId().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDTO.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
