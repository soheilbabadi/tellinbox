package com.tellinbox.tellinbox_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Token Provider for generating, validating, and extracting claims from JWT tokens.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret:TellboxSecretKeyForJWTTokenGenerationMustBeLongEnough2025}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs; // Default 24 hours

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long jwtRefreshExpirationMs; // Default 7 days

    /**
     * Get the signing key from the secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token for user.
     * 
     * @param userDetails user details
     * @return JWT access token
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Include userId in claims if available
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put("userId", customUserDetails.getUserId().toString());
        }
        
        return createToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    /**
     * Generate refresh token for user.
     * 
     * @param userDetails user details
     * @return JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Include userId in claims if available
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put("userId", customUserDetails.getUserId().toString());
        }
        
        return createToken(claims, userDetails.getUsername(), jwtRefreshExpirationMs);
    }

    /**
     * Generate token with custom claims.
     * 
     * @param userDetails user details
     * @param userId user UUID
     * @param role user role
     * @return JWT token
     */
    public String generateTokenWithClaims(UserDetails userDetails, UUID userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("role", role);
        return createToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    /**
     * Create JWT token with claims.
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username from token.
     * 
     * @param token JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token.
     * 
     * @param token JWT token
     * @return user UUID
     */
    public UUID getUserIdFromToken(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return userId != null ? UUID.fromString(userId) : null;
    }

    /**
     * Extract role from token.
     * 
     * @param token JWT token
     * @return role
     */
    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract expiration date from token.
     * 
     * @param token JWT token
     * @return expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract any claim from token.
     * 
     * @param token JWT token
     * @param claimsResolver function to extract claim
     * @return claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token.
     * 
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    /**
     * Validate token.
     * 
     * @param token JWT token
     * @param userDetails user details
     * @return true if valid
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Refresh access token using refresh token.
     * 
     * @param refreshToken refresh token
     * @param userDetails user details
     * @return new access token
     */
    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        if (validateToken(refreshToken, userDetails)) {
            return generateAccessToken(userDetails);
        }
        throw new ResourceUnauthorizedException(getMessage("error.ResourceUnauthorizedException.invalid_refresh_token"));
    }

    /**
     * Get token expiration time in milliseconds.
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Get refresh token expiration time in milliseconds.
     */
    public long getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }

    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
