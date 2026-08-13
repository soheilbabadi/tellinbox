package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.user.dto.GoogleLoginRequest;
import com.tellinbox.tellinbox_api.user.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for Google authentication.
 */
public interface GoogleAuthService {

    /**
     * Authenticate user with Google ID token
     */
    AuthResponse authenticateWithGoogle(GoogleLoginRequest request, HttpServletRequest httpRequest);

    /**
     * Link Google account to existing user
     */
    void linkGoogleAccount(String userId, String idToken);
}
