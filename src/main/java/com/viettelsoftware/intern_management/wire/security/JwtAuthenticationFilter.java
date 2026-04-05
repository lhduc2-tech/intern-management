package com.viettelsoftware.intern_management.wire.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        if (jwtUtil.isTokenValid(jwt)) {
            username = jwtUtil.extractUsername(jwt);
            var claims = jwtUtil.extractAllClaims(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String userId = claims.get("userId", String.class);

                List<String> roles = extractClaimList(claims.get("roles"));
                String legacyRole = claims.get("role", String.class);
                if (legacyRole != null && !legacyRole.isBlank()) {
                    roles = new ArrayList<>(roles);
                    roles.add(legacyRole);
                }

                List<String> permissions = extractClaimList(claims.get("permissions"));

                // Create a principal object that contains both username and userId
                Map<String, String> principal = new HashMap<>();
                principal.put("username", username);
                principal.put("userId", userId);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.addAll(roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                        .collect(Collectors.toList()));
                authorities.addAll(permissions.stream()
                        .filter(permission -> permission != null && !permission.isBlank())
                        .map(permission -> new SimpleGrantedAuthority(permission.trim().toUpperCase()))
                        .collect(Collectors.toList()));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractClaimList(Object claim) {
        if (claim instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }
}
