package com.eventbooking.userservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.eventbooking.userservice.config.MyUserDetails;
import com.eventbooking.userservice.config.MyUserDetailsService;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtService {

    @Autowired
    private MyUserDetailsService userDetailsService;

    public static final String SECRET = System.getenv("JWT_SECRET");

    // A function to generate a JWT token
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // A function to generate a JWT token with claims
    private String createToken(Map<String, Object> claims, String username) {
        MyUserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();

        String rolesClaim = roles.toString();
        log.info("rolesClaim  {} ", rolesClaim);

        int start = rolesClaim.indexOf("[");
        int end = rolesClaim.indexOf("]");
        rolesClaim = rolesClaim.substring(start + 1, end);

        log.info("claims  {} ", rolesClaim);

        // We are adding the roles to the claims
        claims.put("roles", rolesClaim);

        // We are adding the user ID to the claims
        claims.put("userID", userDetails.getUserID());

        // We are adding the username to the claims
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(signingKey(), SignatureAlgorithm.HS256).compact();
    }

    // A function to extract the signing key from the secret
    private Key signingKey() {
        byte[] keyDecoder = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyDecoder);
    }

    // A function to extract the claims from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // A function to extract all the claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // A function to extract the subject of the token (user name)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // A function to extract the user roles/authorities
    public String extractRoles(String token) {
        String claimRoles = extractAllClaims(token).get("roles", String.class);
        return claimRoles;
    }

    // A function to extract the token expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
