package com.tellinbox.tellinbox_api.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tellinbox.tellinbox_api.user.enums.Gender;
import com.tellinbox.tellinbox_api.user.enums.UserRole;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for User operations.
 * Used for API request/response handling.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // === Identification Fields ===
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^09[0-9]{9}$", message = "Invalid mobile number format. Must be 11 digits starting with 09")
    @Size(min = 11, max = 11, message = "Mobile number must be exactly 11 digits")
    private String mobile;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    // === Personal Information ===

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @Size(max = 4000, message = "Bio must be less than 4000 characters")
    private String bio;

    @Size(max = 500, message = "Profile picture URL must be less than 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "Invalid URL format")
    private String profilePictureUrl;

    private Gender gender;

    private LocalDateTime birthDate;

    // === Account Status ===

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Boolean isEmailVerified = false;

    @Builder.Default
    private Boolean isProfileComplete = false;

    // === Security & Role ===

    @Builder.Default
    private UserRole role = UserRole.USER;

    // === Last Login Information ===

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastLoginAt;

    @JsonIgnore
    private String lastLoginIp;

    @JsonIgnore
    private String lastLoginUserAgent;

    // === Preferences ===

    @Builder.Default
    @Size(min = 2, max = 5, message = "Language code must be 2-5 characters")
    private String preferredLanguage = "fa";

    @Builder.Default
    @Size(max = 50, message = "Timezone must be less than 50 characters")
    private String timezone = "Asia/Tehran";

    // === Statistics (Read-only) ===

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private Integer feedbacksCount = 0;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private Double averageScore = 0.0;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private Double trustScore = 0.0;

    // === Timestamps ===

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

    // === Related Entities ===

    private UserProfileDto profile;

    /**
     * Static factory method to convert UserModel to UserDto
     */
    public static UserDto from(UserModel user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
            .id(user.getId())
            .mobile(user.getMobile())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .username(user.getUsername())
            .bio(user.getBio())
            .profilePictureUrl(user.getProfilePictureUrl())
            .gender(user.getGender())
            .birthDate(user.getBirthDate())
            .status(user.getStatus())
            .isVerified(user.getIsVerified())
            .isEmailVerified(user.getIsEmailVerified())
            .isProfileComplete(user.getIsProfileComplete())
            .role(user.getRole())
            .lastLoginAt(user.getLastLoginAt())
            .lastLoginIp(user.getLastLoginIp())
            .lastLoginUserAgent(user.getLastLoginUserAgent())
            .preferredLanguage(user.getPreferredLanguage())
            .timezone(user.getTimezone())
            .feedbacksCount(user.getFeedbacksCount())
            .averageScore(user.getAverageScore())
            .trustScore(user.getTrustScore())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .profile(user.getProfile() != null ? UserProfileDto.from(user.getProfile()) : null)
            .build();
    }

    /**
     * Converts UserDto to UserModel entity
     */
    public UserModel toEntity() {
        UserModel user = UserModel.builder()
            .mobile(this.mobile)
            .email(this.email)
            .fullName(this.fullName)
            .username(this.username)
            .bio(this.bio)
            .profilePictureUrl(this.profilePictureUrl)
            .gender(this.gender)
            .birthDate(this.birthDate)
            .status(this.status != null ? this.status : UserStatus.ACTIVE)
            .isVerified(this.isVerified != null ? this.isVerified : false)
            .isEmailVerified(this.isEmailVerified != null ? this.isEmailVerified : false)
            .isProfileComplete(this.isProfileComplete != null ? this.isProfileComplete : false)
            .role(this.role != null ? this.role : UserRole.USER)
            .preferredLanguage(this.preferredLanguage != null ? this.preferredLanguage : "fa")
            .timezone(this.timezone != null ? this.timezone : "Asia/Tehran")
            .feedbacksCount(this.feedbacksCount != null ? this.feedbacksCount : 0)
            .averageScore(this.averageScore != null ? this.averageScore : 0.0)
            .trustScore(this.trustScore != null ? this.trustScore : 0.0)
            .build();

        // Set ID if present
        if (this.id != null) {
            user.setId(this.id);
        }

        // Set last login info if present
        if (this.lastLoginIp != null) {
            user.setLastLoginIp(this.lastLoginIp);
        }
        if (this.lastLoginUserAgent != null) {
            user.setLastLoginUserAgent(this.lastLoginUserAgent);
        }
        if (this.lastLoginAt != null) {
            user.setLastLoginAt(this.lastLoginAt);
        }

        return user;
    }

    /**
     * Converts UserDto to UserModel for update operations
     * Only updates fields that are not null
     */
    public UserModel toEntityForUpdate(UserModel existingUser) {
        if (existingUser == null) {
            return toEntity();
        }

        if (this.fullName != null) {
            existingUser.setFullName(this.fullName);
        }
        if (this.username != null) {
            existingUser.setUsername(this.username);
        }
        if (this.bio != null) {
            existingUser.setBio(this.bio);
        }
        if (this.profilePictureUrl != null) {
            existingUser.setProfilePictureUrl(this.profilePictureUrl);
        }
        if (this.gender != null) {
            existingUser.setGender(this.gender);
        }
        if (this.birthDate != null) {
            existingUser.setBirthDate(this.birthDate);
        }
        if (this.status != null) {
            existingUser.setStatus(this.status);
        }
        if (this.isVerified != null) {
            existingUser.setIsVerified(this.isVerified);
        }
        if (this.isEmailVerified != null) {
            existingUser.setIsEmailVerified(this.isEmailVerified);
        }
        if (this.isProfileComplete != null) {
            existingUser.setIsProfileComplete(this.isProfileComplete);
        }
        if (this.role != null) {
            existingUser.setRole(this.role);
        }
        if (this.preferredLanguage != null) {
            existingUser.setPreferredLanguage(this.preferredLanguage);
        }
        if (this.timezone != null) {
            existingUser.setTimezone(this.timezone);
        }
        if (this.lastLoginIp != null) {
            existingUser.setLastLoginIp(this.lastLoginIp);
        }
        if (this.lastLoginUserAgent != null) {
            existingUser.setLastLoginUserAgent(this.lastLoginUserAgent);
        }
        if (this.lastLoginAt != null) {
            existingUser.setLastLoginAt(this.lastLoginAt);
        }

        return existingUser;
    }

    // === Helper Methods ===

    /**
     * Gets masked mobile number for privacy
     */
    public String getMaskedMobile() {
        if (this.mobile == null || this.mobile.length() < 11) {
            return this.mobile;
        }
        return this.mobile.substring(0, 3) + "****" + this.mobile.substring(7);
    }

    /**
     * Gets display name (full name or username)
     */
    public String getDisplayName() {
        return this.fullName != null ? this.fullName : 
               this.username != null ? "@" + this.username : 
               "کاربر";
    }

    /**
     * Checks if user is admin
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * Checks if user is moderator
     */
    public boolean isModerator() {
        return this.role == UserRole.MODERATOR || this.role == UserRole.ADMIN;
    }

    /**
     * Checks if user account is active
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}