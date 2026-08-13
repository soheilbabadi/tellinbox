package com.tellinbox.tellinbox_api.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for authentication operations.
 */
@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDto user;
    private String message;
}
