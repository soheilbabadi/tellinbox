package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.security.CustomUserDetails;
import com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse;
import com.tellinbox.tellinbox_api.security.JwtTokenProvider;
import com.tellinbox.tellinbox_api.user.dto.AuthResponse;
import com.tellinbox.tellinbox_api.user.dto.OtpSendRequest;
import com.tellinbox.tellinbox_api.user.dto.OtpVerifyRequest;
import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.enums.LoginMethod;
import com.tellinbox.tellinbox_api.user.model.OtpModel;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.model.UserProfileModel;
import com.tellinbox.tellinbox_api.user.repository.OtpRepository;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 15; // Changed to 15 minutes
    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final String OTP_REDIS_KEY_PREFIX = "otp:";

    @Override
    @Transactional
    public void sendOtp(OtpSendRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getIdentifier();
        String identifierType = request.getIdentifierType();
        String otpTypeStr = request.getOtpType();

        log.info("Sending OTP to {}: {}", identifierType, maskIdentifier(identifier));

        // Rate limiting check using Redis
        String rateLimitKey = OTP_REDIS_KEY_PREFIX + "rate:" + httpRequest.getRemoteAddr();
        Integer recentRequests = (Integer) redisTemplate.opsForValue().get(rateLimitKey);
        if (recentRequests != null && recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new TellInboxCustomException.ResourceForbiddenException(
                "تعداد درخواست‌های شما بیش از حد مجاز است. لطفا بعداً تلاش کنید."
            );
        }

        // Increment rate limit counter in Redis
        if (recentRequests == null) {
            redisTemplate.opsForValue().set(rateLimitKey, 1, 1, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(rateLimitKey);
        }

        // Invalidate previous unused OTPs in database
        otpRepository.findByIdentifierAndIsUsedFalse(identifier)
            .forEach(otp -> {
                otp.setIsUsed(true);
                otpRepository.save(otp);
            });

        // Generate OTP
        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpModel.OtpType otpType = OtpModel.OtpType.valueOf(otpTypeStr);

        OtpModel otp = OtpModel.builder()
            .identifier(identifier)
            .code(otpCode)
            .type(otpType)
            .expiresAt(expiresAt)
            .requestedIp(httpRequest.getRemoteAddr())
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();

        otpRepository.save(otp);

        // Store OTP in Redis with 15 minutes expiry
        String redisOtpKey = OTP_REDIS_KEY_PREFIX + identifier;
        redisTemplate.opsForValue().set(redisOtpKey, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        // Send OTP via email or SMS
        if ("EMAIL".equalsIgnoreCase(identifierType)) {
            sendEmailOtp(identifier, otpCode, otpType);
        } else {
            // For mobile, we would use an SMS service like Kavehnegar, Ghasedak, etc.
            // For now, log the OTP for development purposes
            log.info("SMS OTP for {}: {} (Expires at: {})", identifier, otpCode, expiresAt);
        }

        log.info("OTP sent successfully to {}", identifier);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getIdentifier();
        String code = request.getCode();

        log.info("Verifying OTP for identifier: {}", maskIdentifier(identifier));

        // First, check OTP from Redis (faster)
        String redisOtpKey = OTP_REDIS_KEY_PREFIX + identifier;
        String storedOtp = (String) redisTemplate.opsForValue().get(redisOtpKey);
        
        if (storedOtp == null) {
            // Fallback to database if Redis expired or not found
            OtpModel otp = otpRepository.findByCodeAndIdentifier(code, identifier)
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException("کد تایید نامعتبر است یا منقضی شده است"));

            // Check if OTP is valid
            if (!otp.isValid()) {
                if (otp.isExpired()) {
                    throw new TellInboxCustomException.ResourceNotFoundException("کد تایید منقضی شده است");
                }
                if (otp.getIsUsed()) {
                    throw new TellInboxCustomException.ResourceNotFoundException("کد تایید قبلاً استفاده شده است");
                }
                throw new TellInboxCustomException.ResourceNotFoundException("کد تایید نامعتبر است");
            }

            // Increment attempts
            otp.incrementAttempts();
            if (otp.getAttempts() >= 5) {
                otp.setIsUsed(true); // Block after 5 failed attempts
                otpRepository.save(otp);
                throw new TellInboxCustomException.ResourceForbiddenException(
                    "تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفا کد جدید دریافت کنید."
                );
            }

            // Mark OTP as used
            otp.markAsUsed();
            otpRepository.save(otp);
        } else {
            // Verify OTP from Redis
            if (!storedOtp.equals(code)) {
                throw new TellInboxCustomException.ResourceNotFoundException("کد تایید نامعتبر است");
            }
            
            // Delete OTP from Redis after successful verification (one-time use)
            redisTemplate.delete(redisOtpKey);
        }

        // Authenticate user
        JwtAuthenticationResponse authResponse = userService.authenticateWithOtp(identifier);

        log.info("User authenticated successfully with OTP: {}", maskIdentifier(identifier));

        return AuthResponse.builder()
            .accessToken(authResponse.getAccessToken())
            .refreshToken(authResponse.getRefreshToken())
            .tokenType(authResponse.getTokenType())
            .expiresIn(authResponse.getExpiresIn())
            .user(authResponse.getUser())
            .message("ورود با موفقیت انجام شد")
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
                "لطفا تا ۲ دقیقه دیگر صبر کنید و سپس مجدد درخواست دهید."
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
     * Generate random 6-digit OTP code
     */
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP via email
     */
    private void sendEmailOtp(String to, String otpCode, OtpModel.OtpType otpType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("کد تایید " + otpType.getPersianName());

            String emailContent = String.format("""
                <html>
                <body style="font-family: Tahoma, Arial, sans-serif; direction: rtl; text-align: right;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #333;">کد تایید شما</h2>
                        <p>کاربر گرامی،</p>
                        <p>کد تایید زیر را برای %s وارد نمایید:</p>
                        <div style="background-color: #f4f4f4; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #2196F3; margin: 20px 0;">
                            %s
                        </div>
                        <p>این کد به مدت ۱۵ دقیقه معتبر است.</p>
                        <p>در صورتی که شما این درخواست را انجام نداده‌اید، این ایمیل را نادیده بگیرید.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="color: #666; font-size: 12px;">با احترام، تیم Tellinbox</p>
                    </div>
                </body>
                </html>
                """, otpType.getPersianName(), otpCode);

            helper.setText(emailContent, true);
            mailSender.send(message);

            log.info("OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new TellInboxCustomException.InternalServerErrorException("ارسال کد تایید با خطا مواجه شد");
        }
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
            return parts[0].substring(0, 2) + "***@" + parts[1];
        } else {
            // Mobile masking
            return identifier.substring(0, 3) + "****" + identifier.substring(identifier.length() - 2);
        }
    }

    /**
     * Scheduled task to clean up expired OTPs daily
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupExpiredOtps() {
        int deletedCount = otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up {} expired OTPs", deletedCount);
    }
}
