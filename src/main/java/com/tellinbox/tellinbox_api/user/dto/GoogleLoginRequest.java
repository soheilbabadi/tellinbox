package com.tellinbox.tellinbox_api.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Google login.
 */
@Data
@Builder
public class GoogleLoginRequest {

    /**
     * Google ID token received from client after Google OAuth flow
     */
    private String idToken;

    /**
     * Optional: Access token for additional user info
     */
    private String accessToken;
}
