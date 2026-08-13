package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse;
import com.tellinbox.tellinbox_api.user.dto.AuthResponse;
import com.tellinbox.tellinbox_api.user.dto.OtpSendRequest;
import com.tellinbox.tellinbox_api.user.dto.OtpVerifyRequest;
import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.model.OtpModel;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.OtpRepository;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for OTP operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final MessageSource messageSource;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final String OTP_REDIS_KEY_PREFIX = "otp:";
    private static final String RATE_LIMIT_KEY_PREFIX = "otp:rate:";
    private static final Locale PERSIAN_LOCALE = Locale.forLanguageTag("fa");

    @Override
    @Transactional
    public void sendOtp(OtpSendRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getIdentifier();
        String identifierType = request.getIdentifierType();
        String otpTypeStr = request.getOtpType();

        log.info("Sending OTP to {}: {}", identifierType, maskIdentifier(identifier));

        // Rate limiting check using Redis
        checkRateLimit(httpRequest);

        // Invalidate previous unused OTPs
        invalidatePreviousOtps(identifier);

        // Generate and save OTP
        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        OtpModel.OtpType otpType = OtpModel.OtpType.valueOf(otpTypeStr);

        OtpModel otp = createOtpEntity(identifier, otpCode, otpType, expiresAt, httpRequest);
        otpRepository.save(otp);

        // Store OTP in Redis
        storeOtpInRedis(identifier, otpCode);

        // Send OTP via email or SMS
        sendOtpViaChannel(identifier, identifierType, otpCode, otpType);

        log.info("OTP sent successfully to {}", maskIdentifier(identifier));
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getIdentifier();
        String code = request.getCode();

        log.info("Verifying OTP for identifier: {}", maskIdentifier(identifier));

        // Verify OTP from Redis or database
        validateOtp(identifier, code);

        // Authenticate user and generate tokens
        JwtAuthenticationResponse authResponse = userService.authenticateWithOtp(identifier);
        UserModel user = userRepository.findById(UUID.fromString(authResponse.getUserId()))
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(
                        getMessage("error.ResourceNotFoundException.user_not_found")
                ));

        log.info("User authenticated successfully with OTP: {}", maskIdentifier(identifier));

        return AuthResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(authResponse.getRefreshToken())
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .user(UserDto.from(user))
                .message(getMessage("auth.login.success"))
                .build();
    }

    @Override
    @Transactional
    public void resendOtp(String identifier, HttpServletRequest httpRequest) {
        log.info("Resending OTP to: {}", maskIdentifier(identifier));

        // Check if there's a recent OTP in Redis (within 2 minutes)
        String redisOtpKey = OTP_REDIS_KEY_PREFIX + identifier;
        Boolean exists = redisTemplate.hasKey(redisOtpKey);

        if (Boolean.TRUE.equals(exists)) {
            throw new TellInboxCustomException.ResourceForbiddenException(
                    getMessage("error.ResourceForbiddenException.please_wait_two_minutes")
            );
        }

        // Create and send new OTP
        OtpSendRequest request = OtpSendRequest.builder()
                .identifier(identifier)
                .identifierType(identifier.contains("@") ? "EMAIL" : "MOBILE")
                .otpType("LOGIN")
                .build();

        sendOtp(request, httpRequest);
    }

    /**
     * Scheduled task to clean up expired OTPs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredOtps() {
        int deletedCount = otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up {} expired OTPs", deletedCount);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Check rate limiting for OTP requests
     */
    private void checkRateLimit(HttpServletRequest httpRequest) {
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + httpRequest.getRemoteAddr();
        Integer recentRequests = (Integer) redisTemplate.opsForValue().get(rateLimitKey);

        if (recentRequests != null && recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new TellInboxCustomException.ResourceForbiddenException(
                    getMessage("error.ResourceForbiddenException.too_many_requests")
            );
        }

        if (recentRequests == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1, 1, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(rateLimitKey);
        }
    }

    /**
     * Invalidate previous unused OTPs for the given identifier
     */
    private void invalidatePreviousOtps(String identifier) {
        otpRepository.findByIdentifierAndIsUsedFalse(identifier)
                .forEach(otp -> {
                    otp.setIsUsed(true);
                    otpRepository.save(otp);
                });
    }

    /**
     * Create OTP entity
     */
    private OtpModel createOtpEntity(String identifier, String otpCode, OtpModel.OtpType otpType,
                                     LocalDateTime expiresAt, HttpServletRequest httpRequest) {
        return OtpModel.builder()
                .identifier(identifier)
                .code(otpCode)
                .type(otpType)
                .expiresAt(expiresAt)
                .requestedIp(httpRequest.getRemoteAddr())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();
    }

    /**
     * Store OTP in Redis with expiry
     */
    private void storeOtpInRedis(String identifier, String otpCode) {
        String redisOtpKey = OTP_REDIS_KEY_PREFIX + identifier;
        redisTemplate.opsForValue().set(redisOtpKey, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Send OTP via appropriate channel (email or SMS)
     */
    private void sendOtpViaChannel(String identifier, String identifierType,
                                   String otpCode, OtpModel.OtpType otpType) {
        if ("EMAIL".equalsIgnoreCase(identifierType)) {
            sendEmailOtp(identifier, otpCode, otpType);
        } else {
            // For mobile, use SMS service
            sendSmsOtp(identifier, otpCode);
        }
    }

    /**
     * Validate OTP from Redis or database
     */
    private void validateOtp(String identifier, String code) {
        String redisOtpKey = OTP_REDIS_KEY_PREFIX + identifier;
        String storedOtp = (String) redisTemplate.opsForValue().get(redisOtpKey);

        if (storedOtp != null) {
            // Verify from Redis
            if (!storedOtp.equals(code)) {
                throw new TellInboxCustomException.ResourceNotFoundException(
                        getMessage("error.ResourceNotFoundException.verification_code_invalid")
                );
            }
            redisTemplate.delete(redisOtpKey);
        } else {
            // Fallback to database
            validateOtpFromDatabase(identifier, code);
        }
    }

    /**
     * Validate OTP from database (fallback)
     */
    private void validateOtpFromDatabase(String identifier, String code) {
        OtpModel otp = otpRepository.findByCodeAndIdentifier(code, identifier)
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(
                        getMessage("error.ResourceNotFoundException.verification_code_invalid_or_expired")
                ));

        // Check OTP validity
        if (!otp.isValid()) {
            handleInvalidOtp(otp);
        }

        // Increment attempts
        otp.incrementAttempts();
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otp.setIsUsed(true);
            otpRepository.save(otp);
            throw new TellInboxCustomException.ResourceForbiddenException(
                    getMessage("error.ResourceForbiddenException.too_many_failed_attempts")
            );
        }

        // Mark OTP as used
        otp.markAsUsed();
        otpRepository.save(otp);
    }

    /**
     * Handle invalid OTP cases
     */
    private void handleInvalidOtp(OtpModel otp) {
        if (otp.isExpired()) {
            throw new TellInboxCustomException.ResourceNotFoundException(
                    getMessage("error.ResourceNotFoundException.verification_code_expired")
            );
        }
        if (otp.getIsUsed()) {
            throw new TellInboxCustomException.ResourceNotFoundException(
                    getMessage("error.ResourceNotFoundException.verification_code_already_used")
            );
        }
        throw new TellInboxCustomException.ResourceNotFoundException(
                getMessage("error.ResourceNotFoundException.verification_code_invalid")
        );
    }

    /**
     * Send OTP via email with HTML template
     */
    private void sendEmailOtp(String to, String otpCode, OtpModel.OtpType otpType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(String.format("Verification Code: %s", otpType.getPersianName()));

            String emailContent = buildEmailContent(otpType.getPersianName(), otpCode);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new TellInboxCustomException.ApplicationServerException(
                    getMessage("error.InternalServerErrorException.verification_code_send_failed")
            );
        }
    }

    /**
     * Build HTML email content
     */
    private String buildEmailContent(String otpTypeName, String otpCode) {
        return String.format("""
                <html>
                <body style="font-family: Tahoma, Arial, sans-serif; direction: rtl; text-align: right;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;">
                        <div style="background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            <h2 style="color: #333; margin-top: 0;">کد تایید شما</h2>
                            <p style="color: #555; font-size: 16px;">کاربر گرامی،</p>
                            <p style="color: #555; font-size: 16px;">کد تایید زیر را برای %s وارد نمایید:</p>
                            <div style="background-color: #f4f4f4; padding: 20px; text-align: center; font-size: 28px; font-weight: bold; letter-spacing: 8px; color: #2196F3; margin: 20px 0; border-radius: 5px;">
                                %s
                            </div>
                            <p style="color: #888; font-size: 14px;">⏱ این کد به مدت ۱۵ دقیقه معتبر است.</p>
                            <p style="color: #888; font-size: 14px;">🔒 در صورتی که شما این درخواست را انجام نداده‌اید، این ایمیل را نادیده بگیرید.</p>
                            <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                            <p style="color: #999; font-size: 12px; text-align: center;">با احترام، تیم Tellinbox</p>
                        </div>
                    </div>
                </body>
                </html>
                """, otpTypeName, otpCode);
    }

    /**
     * Send OTP via SMS (placeholder for SMS service integration)
     */
    private void sendSmsOtp(String phoneNumber, String otpCode) {
        // TODO: Integrate with SMS service like Kavenegar, Ghasedak, etc.
        log.info("SMS OTP for {}: {} (Expires in {} minutes)",
                maskIdentifier(phoneNumber), otpCode, OTP_EXPIRY_MINUTES);
    }

    /**
     * Generate random 6-digit OTP code
     */
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Mask identifier for logging (privacy)
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) {
            return "***";
        }
        if (identifier.contains("@")) {
            // Email masking
            String[] parts = identifier.split("@");
            return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
        } else {
            // Mobile masking
            return identifier.substring(0, Math.min(3, identifier.length())) +
                    "****" +
                    identifier.substring(Math.max(0, identifier.length() - 2));
        }
    }

    /**
     * Get localized message from messages.properties
     */
    protected String getMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, PERSIAN_LOCALE);
        } catch (Exception e) {
            log.warn("Message not found for key: {}", key);
            return key;
        }
    }
}