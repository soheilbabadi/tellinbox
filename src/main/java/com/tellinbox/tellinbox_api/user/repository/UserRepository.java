package com.tellinbox.tellinbox_api.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    // ==================== Core Queries ====================

    Optional<UserModel> findByMobile(String mobile);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByUsername(String username);
    Optional<UserModel> findByMobileOrEmail(String mobile, String email);
    
    @Query("SELECT u FROM UserModel u WHERE u.username = :username OR u.mobile = :mobile")
    Optional<UserModel> findByUsernameOrMobile(@Param("username") String username, @Param("mobile") String mobile);

    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // ==================== Status Queries ====================

    List<UserModel> findByStatus(UserStatus status);
    Page<UserModel> findByStatus(UserStatus status, Pageable pageable);
    List<UserModel> findByStatusAndDeletedAtIsNull(UserStatus status);

    // ==================== Date/Time Queries ====================


    // ==================== Custom Queries ====================

    @Query("SELECT u FROM UserModel u WHERE u.isVerified = true AND u.status = 'ACTIVE'")
    List<UserModel> findVerifiedActiveUsers();

    @Query("SELECT u FROM UserModel u WHERE u.feedbacksCount >= :minCount ORDER BY u.averageScore DESC")
    List<UserModel> findTopUsersByFeedbackCount(@Param("minCount") int minCount, Pageable pageable);


    @Query("SELECT u FROM UserModel u WHERE u.feedbacksCount > 0 ORDER BY u.trustScore DESC")
    List<UserModel> findMostTrustedUsers(Pageable pageable);

    @Query("SELECT u FROM UserModel u WHERE u.isProfileComplete = false AND u.createdAt < :createdBefore")
    List<UserModel> findUsersWithIncompleteProfiles(@Param("createdBefore") LocalDateTime createdBefore);


    // ==================== Update Queries ====================

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.status = :status WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") UUID userId, @Param("status") UserStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.isVerified = true WHERE u.mobile = :mobile")
    int verifyUser(@Param("mobile") String mobile);

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.feedbacksCount = u.feedbacksCount + 1, u.averageScore = " +
           "((u.averageScore * u.feedbacksCount) + :newScore) / (u.feedbacksCount + 1) " +
           "WHERE u.id = :userId")
    int incrementFeedbackAndUpdateScore(
        @Param("userId") UUID userId,
        @Param("newScore") Double newScore
    );

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.trustScore = :score WHERE u.id = :userId")
    int updateTrustScore(@Param("userId") UUID userId, @Param("score") Double score);

    // ==================== Deletion Queries ====================

    @Modifying
    @Transactional
    @Query("UPDATE UserModel u SET u.status = 'DELETED', u.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId AND u.status != 'DELETED'")
    int softDeleteUser(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserModel u WHERE u.id = :userId AND u.status = 'DELETED'")
    int hardDeleteUser(@Param("userId") UUID userId);


    @Query("SELECT COUNT(u) FROM UserModel u")
    long getTotalUsers();

    @Query("SELECT COUNT(u) FROM UserModel u WHERE u.status = 'ACTIVE'")
    long getActiveUsers();

    @Query("SELECT COUNT(u) FROM UserModel u WHERE u.isVerified = true")
    long getVerifiedUsers();

    @Query("SELECT AVG(u.feedbacksCount) FROM UserModel u")
    Double getAverageFeedbackCount();

    @Query("SELECT AVG(u.trustScore) FROM UserModel u")
    Double getAverageTrustScore();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM UserModel u " +
           "WHERE u.createdAt > :since GROUP BY DATE(u.createdAt)")
    List<Object[]> getDailyRegistrationStats(@Param("since") LocalDateTime since);

    // ==================== Search Queries ====================

    @Query("SELECT u FROM UserModel u WHERE " +
           "(:query IS NULL OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.bio) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND u.status = 'ACTIVE'")
    Page<UserModel> searchUsers(@Param("query") String query, Pageable pageable);

}