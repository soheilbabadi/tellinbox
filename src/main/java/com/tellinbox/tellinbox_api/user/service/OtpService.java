package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for OTP operations.
 */
public interface OtpService {

    /**
     * Send OTP to user's mobile or email
     */
    void sendOtp(OtpSendRequest request, HttpServletRequest httpRequest);

    /**
     * Verify OTP and return authentication tokens
     */
    AuthResponse verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest);

    /**
     * Resend OTP (with rate limiting)
     */
    void resendOtp(String identifier, HttpServletRequest httpRequest);
}
