package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.user.dto.UpdateProfileRequest;
import com.tellinbox.tellinbox_api.user.dto.UserDto;
import com.tellinbox.tellinbox_api.user.dto.UserProfileDto;
import com.tellinbox.tellinbox_api.user.dto.UserRegistrationRequest;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user operations.
 * Defines the contract for user management functionality.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
public interface UserService {

    // ==================== Core CRUD Operations ====================

    /**
     * Register a new user
     * @param request Registration request with user details
     * @return Created user DTO
     */
    UserDto registerUser(UserRegistrationRequest request);

    /**
     * Find user by ID
     * @param id User UUID
     * @return Optional containing user if found
     */
    Optional<UserDto> findById(UUID id);

    /**
     * Find user by mobile number
     * @param mobile Mobile number
     * @return Optional containing user if found
     */
    Optional<UserDto> findByMobile(String mobile);

    /**
     * Find user by email
     * @param email Email address
     * @return Optional containing user if found
     */
    Optional<UserDto> findByEmail(String email);

    /**
     * Find user by username
     * @param username Username
     * @return Optional containing user if found
     */
    Optional<UserDto> findByUsername(String username);

    /**
     * Get all users with pagination
     * @param pageable Pagination information
     * @return Page of user DTOs
     */
    Page<UserDto> findAll(Pageable pageable);

    /**
     * Update user profile
     * @param userId User ID
     * @param dto User profile DTO with updated information
     * @return Updated user DTO
     */
    UserDto updateUser(UUID userId, UserProfileDto dto);

    /**
     * Delete user (soft delete)
     * @param userId User ID to delete
     */
    void deleteUser(UUID userId);

    /**
     * Permanently delete user (hard delete)
     * @param userId User ID to permanently delete
     */
    void hardDeleteUser(UUID userId);

    // ==================== Status Management ====================

    /**
     * Update user status
     * @param userId User ID
     * @param status New status
     * @return Updated user DTO
     */
    UserDto updateUserStatus(UUID userId, UserStatus status);

    /**
     * Verify user's mobile number
     * @param userId User ID
     * @return Updated user DTO
     */
    UserDto verifyUser(UUID userId);

    /**
     * Verify user's email
     * @param userId User ID
     * @return Updated user DTO
     */
    UserDto verifyEmail(UUID userId);

    /**
     * Get users by status
     * @param status User status
     * @return List of user DTOs
     */
    List<UserDto> getUsersByStatus(UserStatus status);

    // ==================== Authentication & Security ====================

    /**
     * Update user password
     * @param userId User ID
     * @param currentPassword Current password for verification
     * @param newPassword New password
     */
    void updatePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * Reset user password (admin operation)
     * @param userId User ID
     * @param newPassword New password
     */
    void resetPassword(UUID userId, String newPassword);

    /**
     * Update last login information
     * @param userId User ID
     * @param ip IP address
     * @param userAgent User agent string
     */
    void updateLastLogin(UUID userId, String ip, String userAgent);

    // ==================== Statistics & Analytics ====================

    /**
     * Get total number of users
     * @return Total user count
     */
    long getTotalUsers();

    /**
     * Get number of active users
     * @return Active user count
     */
    long getActiveUsers();

    /**
     * Get number of verified users
     * @return Verified user count
     */
    long getVerifiedUsers();

    /**
     * Get average feedback count per user
     * @return Average feedback count
     */
    Double getAverageFeedbackCount();

    /**
     * Get average trust score
     * @return Average trust score
     */
    Double getAverageTrustScore();

    /**
     * Get daily registration statistics
     * @param since Start date
     * @return List of date-count pairs
     */
    List<Object[]> getDailyRegistrationStats(LocalDateTime since);

    // ==================== Search & Advanced Queries ====================

    /**
     * Search users by query
     * @param query Search query
     * @param pageable Pagination information
     * @return Page of matching users
     */
    Page<UserDto> searchUsers(String query, Pageable pageable);

    /**
     * Get top users by feedback count
     * @param minCount Minimum feedback count
     * @param pageable Pagination information
     * @return List of top users
     */
    List<UserDto> getTopUsersByFeedbackCount(int minCount, Pageable pageable);

    /**
     * Get most trusted users
     * @param pageable Pagination information
     * @return List of trusted users
     */
    List<UserDto> getMostTrustedUsers(Pageable pageable);

    /**
     * Get verified active users
     * @return List of verified active users
     */
    List<UserDto> getVerifiedActiveUsers();

    /**
     * Find users with incomplete profiles
     * @param createdBefore Users created before this date
     * @return List of users with incomplete profiles
     */
    List<UserDto> getUsersWithIncompleteProfiles(LocalDateTime createdBefore);

    // ==================== Authentication ====================

    /**
     * Authenticate user and generate tokens
     * @param usernameOrMobile username or mobile number
     * @param password user password
     * @return JWT authentication response
     */
    com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse authenticate(String usernameOrMobile, String password);

    /**
     * Refresh access token using refresh token
     * @param refreshToken refresh token
     * @return new JWT authentication response
     */
    com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse refreshToken(String refreshToken);

    /**
     * Get current user profile
     * @param userId user ID
     * @return user DTO
     */
    UserDto getProfile(UUID userId);

    /**
     * Update current user profile
     * @param userId user ID
     * @param request update profile request
     * @return updated user DTO
     */
    UserDto updateProfile(UUID userId, UpdateProfileRequest request);

    // ==================== OTP Authentication ====================

    /**
     * Authenticate user with OTP and generate tokens
     * @param mobile mobile number
     * @return JWT authentication response
     */
    com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse authenticateWithOtp(String mobile);

    // ==================== Google Authentication ====================

    /**
     * Authenticate user with Google token and generate tokens
     * @param googleIdToken Google ID token
     * @return JWT authentication response
     */
    com.tellinbox.tellinbox_api.security.JwtAuthenticationResponse authenticateWithGoogle(String googleIdToken);
}
