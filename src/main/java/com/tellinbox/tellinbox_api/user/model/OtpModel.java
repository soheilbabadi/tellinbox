package com.tellinbox.tellinbox_api.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class for storing OTP codes.
 * Used for phone/email verification and login.
 */
@Entity
@Table(
    name = "otp_codes",
    indexes = {
        @Index(name = "idx_otp_identifier", columnList = "identifier"),
        @Index(name = "idx_otp_type", columnList = "type"),
        @Index(name = "idx_otp_expires_at", columnList = "expiresAt"),
        @Index(name = "idx_otp_created_at", columnList = "createdAt")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OtpModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Identifier for the OTP (mobile number or email)
     */
    @Column(name = "identifier", nullable = false, length = 100)
    private String identifier;

    /**
     * The OTP code (6 digits for SMS, 6-8 for email)
     */
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    /**
     * Type of OTP
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private OtpType type = OtpType.LOGIN;

    /**
     * Expiration time
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether the OTP has been used
     */
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    /**
     * When the OTP was used
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Number of verification attempts
     */
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    /**
     * IP address that requested the OTP
     */
    @Column(name = "requested_ip", length = 45)
    private String requestedIp;

    /**
     * User agent of the request
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Check if OTP is valid (not expired and not used)
     */
    public boolean isValid() {
        return !this.isUsed && !isExpired() && this.attempts < 5;
    }

    /**
     * Mark OTP as used
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Increment attempt counter
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * OTP types
     */
    public enum OtpType {
        LOGIN("ورود"),
        REGISTRATION("ثبت‌نام"),
        PASSWORD_RESET("بازیابی رمز عبور"),
        EMAIL_VERIFICATION("تایید ایمیل"),
        MOBILE_VERIFICATION("تایید شماره موبایل");

        private final String persianName;

        OtpType(String persianName) {
            this.persianName = persianName;
        }

        public String getPersianName() {
            return persianName;
        }
    }
}
