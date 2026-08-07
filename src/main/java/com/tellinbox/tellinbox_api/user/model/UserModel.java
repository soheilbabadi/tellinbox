package com.tellinbox.tellinbox_api.user.model;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.user.enums.UserRole;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity class representing a user in the Tellinbox system.
 * This is the core user model that stores all user-related data.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_mobile", columnNames = "mobile"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    },
    indexes = {
        @Index(name = "idx_users_mobile", columnList = "mobile"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_status", columnList = "status"),
        @Index(name = "idx_users_created_at", columnList = "createdAt"),
        @Index(name = "idx_users_is_verified", columnList = "isVerified")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"password", "profile"})
@EqualsAndHashCode(callSuper = false)
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    protected Boolean isDeleted = false;

    /**
     * Mobile number - unique identifier for login
     * Format: 09XXXXXXXXX (Iranian mobile numbers)
     */
    @Column(
        name = "mobile",
        nullable = false,
        unique = true,
        length = 11
    )
    private String mobile;

    /**
     * Email address - optional but recommended
     */
    @Column(
        name = "email",
        unique = true,
        length = 100
    )
    private String email;

    /**
     * Hashed password - only for email/password authentication
     * Not used for OTP-based login
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /**
     * Full name of the user
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Unique username for public profile
     * Example: @soheil_ahmadi
     */
    @Column(
        name = "username",
        unique = true,
        length = 50
    )
    private String username;

    /**
     * Short bio/description about the user
     */
    @Column(name = "bio", length=4000)
    private String bio;

    /**
     * URL to user's avatar image (stored in cloud storage)
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * User's gender - optional
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private com.tellinbox.tellinbox_api.user.enums.Gender gender;

    /**
     * User's birth date - optional for age calculation
     */
    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    /**
     * User account status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private com.tellinbox.tellinbox_api.user.enums.UserStatus status = UserStatus.ACTIVE;

    /**
     * Whether the user's mobile number is verified
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * Whether the user's email is verified
     */
    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    /**
     * Whether the user has completed their profile
     */
    @Column(name = "is_profile_complete", nullable = false)
    @Builder.Default
    private Boolean isProfileComplete = false;

    /**
     * Last login timestamp
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * IP address of last login
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * User agent of last login
     */
    @Column(name = "last_login_user_agent", length = 500)
    private String lastLoginUserAgent;

    /**
     * Language preference (fa/en)
     */
    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "fa";

    /**
     * Timezone
     */
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Tehran";

    /**
     * Number of feedbacks received
     * Denormalized for performance
     */
    @Column(name = "feedbacks_count")
    @Builder.Default
    private Integer feedbacksCount = 0;

    /**
     * Average score of all feedbacks
     * Denormalized for performance
     */
    @Column(name = "average_score")
    @Builder.Default
    private Double averageScore = 0.0;

    /**
     * Trust score calculated by system
     */
    @Column(name = "trust_score")
    @Builder.Default
    private Double trustScore = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * One-to-One relationship with UserProfile
     * Using fetch = LAZY to improve performance
     */
    @OneToOne(
        mappedBy = "user",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.LAZY,
        optional = false,
        orphanRemoval = true
    )
    @PrimaryKeyJoinColumn
    private UserProfileModel profile;

    /**
     * One-to-Many relationship with Feedback (received)
     */
    @OneToMany(
        mappedBy = "receiver",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeedbackModel> receivedFeedbacks = new ArrayList<>();

    /**
     * One-to-Many relationship with Feedback (given)
     */
    @OneToMany(
        mappedBy = "author",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeedbackModel> givenFeedbacks = new ArrayList<>();


    /**
     * Helper method to increment feedback count
     */
    public void incrementFeedbackCount() {
        this.feedbacksCount = (this.feedbacksCount == null ? 0 : this.feedbacksCount) + 1;
    }

    /**
     * Helper method to update average score
     */
    public void updateAverageScore(Double newScore) {
        if (this.feedbacksCount == null || this.feedbacksCount == 0) {
            this.averageScore = newScore;
        } else {
            // Weighted average calculation
            double totalScore = this.averageScore * this.feedbacksCount;
            this.averageScore = (totalScore + newScore) / (this.feedbacksCount + 1);
        }
        // Round to 2 decimal places
        this.averageScore = Math.round(this.averageScore * 100.0) / 100.0;
    }

    /**
     * Check if user account is active
     */
    public boolean isActive() {
        return this.status == com.tellinbox.tellinbox_api.user.enums.UserStatus.ACTIVE && !Boolean.TRUE.equals(this.isDeleted);
    }

    /**
     * Check if user can receive feedback
     */
    public boolean canReceiveFeedback() {
        return this.isActive() && 
               this.profile != null && 
               this.profile.getReceiveAnonymousFeedback();
    }

    /**
     * Update last login information
     */
    public void updateLastLogin(String ip, String userAgent) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.lastLoginUserAgent = userAgent;
    }

    /**
     * Soft delete user
     */
    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted user
     */
    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if user's profile is complete
     */
    public boolean isProfileComplete() {
        return this.fullName != null && 
               this.username != null && 
               this.avatarUrl != null && 
               this.bio != null;
    }

    /**
     * Get public display name
     */
    public String getDisplayName() {
        return this.fullName != null ? this.fullName : this.username != null ? "@" + this.username : "کاربر";
    }

    /**
     * Builder with defaults
     */
    public static class UserBuilder {
        private UserStatus status = UserStatus.ACTIVE;
        private Boolean isVerified = false;
        private Boolean isEmailVerified = false;
        private Boolean isProfileComplete = false;
        private String preferredLanguage = "fa";
        private String timezone = "Asia/Tehran";
        private Integer feedbacksCount = 0;
        private Double averageScore = 0.0;
        private Double trustScore = 0.0;
    }
}