package com.tellinbox.tellinbox_api.user.controller;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.security.CustomUserDetails;
import com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse;
import com.tellinbox.tellinbox_api.user.dto.LoginRequest;
import com.tellinbox.tellinbox_api.user.dto.UpdateProfileRequest;
import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.dto.UserRegistrationRequest;
import com.tellinbox.tellinbox_api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for authentication and user management operations.
 * Handles login, registration, profile management, and token refresh.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MessageSource messageSource;
    private final UserService userService;

    /**
     * Register a new user.
     * 
     * @param request registration request
     * @return created user DTO
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserDto user = userService.registerUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Login with username/mobile and password.
     * 
     * @param request login request with credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = userService.authenticate(
            request.getUsernameOrMobile(), 
            request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token.
     * 
     * @param requestBody contains refresh token
     * @return new JWT tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        JwtAuthenticationResponse response = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user's profile.
     * 
     * @param userDetails authenticated user details
     * @return user profile DTO
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getCurrentUserId(userDetails);
        UserDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current authenticated user's profile.
     * 
     * @param request update profile request
     * @param userDetails authenticated user details
     * @return updated user profile DTO
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getCurrentUserId(userDetails);
        UserDto updatedProfile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Helper method to extract user ID from authenticated user details.
     */
    private UUID getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }
        throw new TellInboxCustomException.ApplicationServerException(getMessage("error.IllegalStateException.unable_to_extract_user_id_from_authentication_context"));
    }

    /**
     * Get localized message from messages.properties
     * @param key Message key
     * @param args Optional arguments for message formatting
     * @return Localized message
     */
    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
