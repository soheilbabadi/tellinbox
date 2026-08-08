package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.dto.UserProfileDto;
import com.tellinbox.tellinbox_api.user.dto.UserRegistrationRequest;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.model.UserProfileModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for user operations.
 * Provides business logic for user management.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== Core CRUD Operations ====================

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with mobile: {}", request.getMobile());

        // Check if mobile already exists
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("کاربری با این شماره موبایل وجود دارد");
        }

        // Check if email already exists (if provided)
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("کاربری با این ایمیل وجود دارد");
        }

        // Set default profile picture if not provided
        String profilePictureUrl = request.getProfilePictureUrl();
        if (profilePictureUrl == null || profilePictureUrl.isBlank()) {
            profilePictureUrl = "https://picsum.photos/200";
        }

        // Build user entity
        UserModel user = UserModel.builder()
            .mobile(request.getMobile())
            .email(request.getEmail())
            .fullName(request.getFullName())
            .username(request.getUsername())
            .bio(request.getBio())
            .profilePictureUrl(profilePictureUrl)
            .preferredLanguage(request.getPreferredLanguage())
            .status(UserStatus.ACTIVE)
            .isVerified(false)
            .isEmailVerified(false)
            .isProfileComplete(false)
            .feedbacksCount(0)
            .averageScore(0.0)
            .trustScore(0.0)
            .build();

        // Create default profile
        UserProfileModel profile = UserProfileModel.builder()
            .user(user)
            .receiveAnonymousFeedback(true)
            .receiveNamedFeedback(true)
            .showStatistics(true)
            .showAverageScore(true)
            .enableAiAnalysis(true)
            .receiveEmailNotifications(true)
            .receiveSmsNotifications(true)
            .receivePushNotifications(true)
            .itemsPerPage(20)
            .theme("light")
            .build();

        user.setProfile(profile);

        // Save user
        UserModel savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return UserDto.from(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByMobile(String mobile) {
        log.debug("Finding user by mobile: {}", mobile);
        return userRepository.findByMobile(mobile).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        log.debug("Finding all users with pagination: {}", pageable);
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID userId, UserProfileDto dto) {
        log.info("Updating user profile for ID: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        // Update fields
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthDate() != null) {
            user.setBirthDate(dto.getBirthDate());
        }

        // Check if profile is complete
        user.setIsProfileComplete(user.isProfileComplete());

        UserModel updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for ID: {}", userId);

        return UserDto.from(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user with ID: {}", userId);
        
        int deleted = userRepository.softDeleteUser(userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("کاربر یافت نشد یا قبلاً حذف شده است");
        }
        
        log.info("User soft deleted successfully: {}", userId);
    }

    @Override
    @Transactional
    public void hardDeleteUser(UUID userId) {
        log.info("Hard deleting user with ID: {}", userId);
        
        // First soft delete if not already deleted
        userRepository.softDeleteUser(userId);
        
        int deleted = userRepository.hardDeleteUser(userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("کاربر یافت نشد");
        }
        
        log.info("User hard deleted successfully: {}", userId);
    }

    // ==================== Status Management ====================

    @Override
    @Transactional
    public UserDto updateUserStatus(UUID userId, UserStatus status) {
        log.info("Updating user status for ID: {} to {}", userId, status);

        int updated = userRepository.updateUserStatus(userId, status);
        if (updated == 0) {
            throw new IllegalArgumentException("کاربر یافت نشد");
        }

        return findById(userId).orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));
    }

    @Override
    @Transactional
    public UserDto verifyUser(UUID userId) {
        log.info("Verifying user with ID: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        user.setIsVerified(true);
        UserModel updatedUser = userRepository.save(user);

        log.info("User verified successfully: {}", userId);
        return UserDto.from(updatedUser);
    }

    @Override
    @Transactional
    public UserDto verifyEmail(UUID userId) {
        log.info("Verifying email for user with ID: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        user.setIsEmailVerified(true);
        UserModel updatedUser = userRepository.save(user);

        log.info("Email verified successfully: {}", userId);
        return UserDto.from(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByStatus(UserStatus status) {
        log.debug("Finding users by status: {}", status);
        return userRepository.findByStatusAndDeletedAtIsNull(status)
            .stream()
            .map(UserDto::from)
            .collect(Collectors.toList());
    }

    // ==================== Authentication & Security ====================

    @Override
    @Transactional
    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Updating password for user with ID: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        // Verify current password if exists
        if (user.getPasswordHash() != null) {
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new IllegalArgumentException("رمز عبور فعلی اشتباه است");
            }
        }

        // Encode and save new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password updated successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void resetPassword(UUID userId, String newPassword) {
        log.info("Resetting password for user with ID: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId, String ip, String userAgent) {
        log.debug("Updating last login for user: {}", userId);

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد"));

        user.updateLastLogin(ip, userAgent);
        userRepository.save(user);
    }

    // ==================== Statistics & Analytics ====================

    @Override
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.getTotalUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUsers() {
        return userRepository.getActiveUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public long getVerifiedUsers() {
        return userRepository.getVerifiedUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageFeedbackCount() {
        return userRepository.getAverageFeedbackCount();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageTrustScore() {
        return userRepository.getAverageTrustScore();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyRegistrationStats(LocalDateTime since) {
        return userRepository.getDailyRegistrationStats(since);
    }

    // ==================== Search & Advanced Queries ====================

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String query, Pageable pageable) {
        log.debug("Searching users with query: {}", query);
        return userRepository.searchUsers(query, pageable).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getTopUsersByFeedbackCount(int minCount, Pageable pageable) {
        log.debug("Finding top users by feedback count with min: {}", minCount);
        return userRepository.findTopUsersByFeedbackCount(minCount, pageable)
            .stream()
            .map(UserDto::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getMostTrustedUsers(Pageable pageable) {
        log.debug("Finding most trusted users");
        return userRepository.findMostTrustedUsers(pageable)
            .stream()
            .map(UserDto::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getVerifiedActiveUsers() {
        log.debug("Finding verified active users");
        return userRepository.findVerifiedActiveUsers()
            .stream()
            .map(UserDto::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithIncompleteProfiles(LocalDateTime createdBefore) {
        log.debug("Finding users with incomplete profiles created before: {}", createdBefore);
        return userRepository.findUsersWithIncompleteProfiles(createdBefore)
            .stream()
            .map(UserDto::from)
            .collect(Collectors.toList());
    }
}
