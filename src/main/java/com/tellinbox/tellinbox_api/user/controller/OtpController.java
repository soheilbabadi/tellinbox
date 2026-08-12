package com.tellinbox.tellinbox_api.user.controller;

import com.tellinbox.tellinbox_api.user.dto.*;
import com.tellinbox.tellinbox_api.user.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for OTP-based authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    /**
     * Request OTP to be sent to mobile or email
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendOtp(
            @Valid @RequestBody OtpSendRequest request,
            HttpServletRequest httpRequest) {
        log.info("OTP send request received for: {}", request.getIdentifier());
        otpService.sendOtp(request, httpRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Verify OTP and login
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpRequest) {
        log.info("OTP verify request received for: {}", request.getIdentifier());
        AuthResponse response = otpService.verifyOtp(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Resend OTP (with rate limiting)
     */
    @PostMapping("/resend")
    public ResponseEntity<Void> resendOtp(
            @RequestParam String identifier,
            HttpServletRequest httpRequest) {
        log.info("OTP resend request received for: {}", identifier);
        otpService.resendOtp(identifier, httpRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
