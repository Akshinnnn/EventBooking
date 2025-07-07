package com.eventbooking.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {
    public static final String SECRET = System.getenv("JWT_SECRET");

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // A function to extract the username from the token
    public String extractUsername(String token) {
        token = getTokenFromHeader(token);
        return extractClaim(token, Claims::getSubject);
    }

    // A function to extract the user ID from the token
    public UUID extractUserID(String token) {
        token = getTokenFromHeader(token);
        String userID = extractClaim(token, claims -> claims.get("userID", String.class));
        log.info("Extracting userID from token: {}", userID);
        return UUID.fromString(userID);
    }

    // A function to extract the role from the token
    public String extractRoles(String token) {
        token = getTokenFromHeader(token);
        String roles = extractClaim(token, claims -> claims.get("roles", String.class));
        log.info("Extracting role from token: {}", roles);
        return roles;
    }

    // A function to extract the claims from the token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        token = getTokenFromHeader(token);
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // A function to extract all the claims from the token
    private Claims extractAllClaims(String token) {
        token = getTokenFromHeader(token);
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Get token from authorization header
    public String getTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }

    // A function to check if the token is valid
    public boolean isAuthenticated(String token) {
        token = getTokenFromHeader(token);
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // A function to check if the user has a specific role
    public boolean isAuthorized(String token, String role) {
        token = getTokenFromHeader(token);
        String roles = extractRoles(token);
        return roles != null && roles.contains(role);
    }
}
