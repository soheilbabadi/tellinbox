package com.tellinbox.tellinbox_api.user.mapper;

import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between UserModel and User DTOs.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
public class UserMapper {

    /**
     * Converts a UserDto to a UserModel entity.
     * 
     * @param dto the user DTO
     * @return the created UserModel entity, or null if dto is null
     */
    public UserModel toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        UserModel user = UserModel.builder()
                .mobile(dto.getMobile())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .bio(dto.getBio())
                .profilePictureUrl(dto.getProfilePictureUrl())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .status(dto.getStatus() != null ? dto.getStatus() : com.tellinbox.tellinbox_api.user.enums.UserStatus.ACTIVE)
                .isVerified(dto.getIsVerified() != null ? dto.getIsVerified() : false)
                .isEmailVerified(dto.getIsEmailVerified() != null ? dto.getIsEmailVerified() : false)
                .isProfileComplete(dto.getIsProfileComplete() != null ? dto.getIsProfileComplete() : false)
                .role(dto.getRole() != null ? dto.getRole() : com.tellinbox.tellinbox_api.user.enums.UserRole.USER)
                .preferredLanguage(dto.getPreferredLanguage() != null ? dto.getPreferredLanguage() : "fa")
                .timezone(dto.getTimezone() != null ? dto.getTimezone() : "Asia/Tehran")
                .feedbacksCount(dto.getFeedbacksCount() != null ? dto.getFeedbacksCount() : 0)
                .averageScore(dto.getAverageScore() != null ? dto.getAverageScore() : 0.0)
                .trustScore(dto.getTrustScore() != null ? dto.getTrustScore() : 0.0)
                .build();

        // Set ID if present
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }

        // Set last login info if present
        if (dto.getLastLoginIp() != null) {
            user.setLastLoginIp(dto.getLastLoginIp());
        }
        if (dto.getLastLoginUserAgent() != null) {
            user.setLastLoginUserAgent(dto.getLastLoginUserAgent());
        }
        if (dto.getLastLoginAt() != null) {
            user.setLastLoginAt(dto.getLastLoginAt());
        }

        return user;
    }

    /**
     * Converts a UserModel entity to a UserDto.
     * 
     * @param user the user entity
     * @return the user DTO, or null if user is null
     */
    public UserDto toDto(UserModel user) {
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
                .build();
    }

    /**
     * Updates an existing UserModel with fields from UserDto.
     * Only non-null fields in the DTO are updated.
     * 
     * @param dto the user DTO with updated values
     * @param existingUser the existing user entity to update
     * @return the updated UserModel entity
     */
    public UserModel updateEntity(UserDto dto, UserModel existingUser) {
        if (existingUser == null || dto == null) {
            return null;
        }

        if (dto.getFullName() != null) {
            existingUser.setFullName(dto.getFullName());
        }
        if (dto.getUsername() != null) {
            existingUser.setUsername(dto.getUsername());
        }
        if (dto.getBio() != null) {
            existingUser.setBio(dto.getBio());
        }
        if (dto.getProfilePictureUrl() != null) {
            existingUser.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getGender() != null) {
            existingUser.setGender(dto.getGender());
        }
        if (dto.getBirthDate() != null) {
            existingUser.setBirthDate(dto.getBirthDate());
        }
        if (dto.getStatus() != null) {
            existingUser.setStatus(dto.getStatus());
        }
        if (dto.getIsVerified() != null) {
            existingUser.setIsVerified(dto.getIsVerified());
        }
        if (dto.getIsEmailVerified() != null) {
            existingUser.setIsEmailVerified(dto.getIsEmailVerified());
        }
        if (dto.getIsProfileComplete() != null) {
            existingUser.setIsProfileComplete(dto.getIsProfileComplete());
        }
        if (dto.getRole() != null) {
            existingUser.setRole(dto.getRole());
        }
        if (dto.getPreferredLanguage() != null) {
            existingUser.setPreferredLanguage(dto.getPreferredLanguage());
        }
        if (dto.getTimezone() != null) {
            existingUser.setTimezone(dto.getTimezone());
        }
        if (dto.getLastLoginIp() != null) {
            existingUser.setLastLoginIp(dto.getLastLoginIp());
        }
        if (dto.getLastLoginUserAgent() != null) {
            existingUser.setLastLoginUserAgent(dto.getLastLoginUserAgent());
        }
        if (dto.getLastLoginAt() != null) {
            existingUser.setLastLoginAt(dto.getLastLoginAt());
        }

        return existingUser;
    }
}
