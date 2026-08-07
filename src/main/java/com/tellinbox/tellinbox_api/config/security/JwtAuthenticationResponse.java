package com.tellinbox.tellinbox_api.config.security;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for JWT authentication response containing access and refresh tokens.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
public class JwtAuthenticationResponse {
    
    /**
     * JWT access token
     */
    private String accessToken;
    
    /**
     * JWT refresh token
     */
    private String refreshToken;
    
    /**
     * Token type (usually "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Access token expiration time in milliseconds
     */
    private Long expiresIn;
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * User role
     */
    private String role;
}
