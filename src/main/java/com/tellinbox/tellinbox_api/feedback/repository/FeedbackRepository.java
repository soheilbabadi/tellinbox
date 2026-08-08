package com.tellinbox.tellinbox_api.feedback.repository;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Feedback operations.
 * Provides database access methods for feedback entities.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackModel, UUID> {

    // ==================== Core Queries ====================

    /**
     * Find feedbacks by receiver ID
     */
    Page<FeedbackModel> findByReceiverId(UUID receiverId, Pageable pageable);

    /**
     * Find feedbacks by author ID
     */
    Page<FeedbackModel> findByAuthorId(UUID authorId, Pageable pageable);

    /**
     * Find feedbacks by receiver and status
     */
    Page<FeedbackModel> findByReceiverIdAndStatus(UUID receiverId, FeedbackStatus status, Pageable pageable);

    /**
     * Find feedbacks by receiver and visibility
     */
    Page<FeedbackModel> findByReceiverIdAndVisibility(UUID receiverId, FeedbackVisibility visibility, Pageable pageable);

    /**
     * Count feedbacks by receiver
     */
    long countByReceiverId(UUID receiverId);

    /**
     * Count feedbacks by receiver and status
     */
    long countByReceiverIdAndStatus(UUID receiverId, FeedbackStatus status);

    // ==================== Status Queries ====================

    /**
     * Find pending feedbacks for a receiver
     */
    List<FeedbackModel> findByReceiverIdAndStatus(UUID receiverId, FeedbackStatus status);

    /**
     * Find published feedbacks for a receiver
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :receiverId AND f.status = :status")
    Page<FeedbackModel> findPublishedByReceiver(
        @Param("receiverId") UUID receiverId,
        @Param("status") FeedbackStatus status,
        Pageable pageable
    );

    /**
     * Find flagged feedbacks
     */
    List<FeedbackModel> findByIsFlaggedTrue();

    /**
     * Find feedbacks with reports
     */
    List<FeedbackModel> findByReportCountGreaterThan(int minReports);

    // ==================== Date/Time Queries ====================

    /**
     * Find feedbacks created in date range
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<FeedbackModel> findFeedbacksInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find feedbacks created after a specific date
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.createdAt > :since")
    List<FeedbackModel> findFeedbacksSince(@Param("since") LocalDateTime since);

    // ==================== Custom Queries ====================

    /**
     * Find recent active feedbacks for a user
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :userId " +
           "AND f.status = 'PUBLISHED' AND f.isDeleted = false " +
           "ORDER BY f.createdAt DESC")
    Page<FeedbackModel> findRecentActiveFeedbacks(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * Find unread feedbacks for a user
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :userId " +
           "AND f.isRead = false AND f.isDeleted = false")
    List<FeedbackModel> findUnreadFeedbacks(@Param("userId") UUID userId);

    /**
     * Find feedbacks with responses
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :userId " +
           "AND f.hasResponse = true AND f.isDeleted = false")
    Page<FeedbackModel> findFeedbacksWithResponses(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * Find anonymous feedbacks
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.isAnonymous = true AND f.receiver.id = :userId")
    List<FeedbackModel> findAnonymousFeedbacks(@Param("userId") UUID userId);

    /**
     * Search feedbacks by content
     */
    @Query("SELECT f FROM FeedbackModel f WHERE " +
           "(LOWER(f.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.title) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND f.receiver.id = :userId AND f.isDeleted = false")
    Page<FeedbackModel> searchFeedbacks(
        @Param("userId") UUID userId,
        @Param("query") String query,
        Pageable pageable
    );

    // ==================== Update Queries ====================

    /**
     * Mark feedback as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.isRead = true, f.readAt = CURRENT_TIMESTAMP " +
           "WHERE f.id = :feedbackId")
    int markAsRead(@Param("feedbackId") UUID feedbackId);

    /**
     * Update feedback status
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.status = :status WHERE f.id = :feedbackId")
    int updateStatus(@Param("feedbackId") UUID feedbackId, @Param("status") FeedbackStatus status);

    /**
     * Update feedback visibility
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.visibility = :visibility WHERE f.id = :feedbackId")
    int updateVisibility(@Param("feedbackId") UUID feedbackId, @Param("visibility") FeedbackVisibility visibility);

    /**
     * Publish feedback
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.status = 'PUBLISHED', f.publishedAt = CURRENT_TIMESTAMP " +
           "WHERE f.id = :feedbackId")
    int publishFeedback(@Param("feedbackId") UUID feedbackId);

    /**
     * Archive feedback
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.status = 'ARCHIVED', f.archivedAt = CURRENT_TIMESTAMP, " +
           "f.archivedBy = :archivedBy WHERE f.id = :feedbackId")
    int archiveFeedback(@Param("feedbackId") UUID feedbackId, @Param("archivedBy") UUID archivedBy);

    /**
     * Soft delete feedback
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.status = 'DELETED', f.deletedAt = CURRENT_TIMESTAMP, " +
           "f.isDeleted = true WHERE f.id = :feedbackId")
    int softDeleteFeedback(@Param("feedbackId") UUID feedbackId);

    /**
     * Increment report count and flag if needed
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.reportCount = f.reportCount + 1, " +
           "f.isFlagged = CASE WHEN f.reportCount >= 4 THEN true ELSE f.isFlagged END " +
           "WHERE f.id = :feedbackId")
    int incrementReportCount(@Param("feedbackId") UUID feedbackId);

    /**
     * Set hasResponse flag
     */
    @Modifying
    @Transactional
    @Query("UPDATE FeedbackModel f SET f.hasResponse = true WHERE f.id = :feedbackId")
    int setHasResponse(@Param("feedbackId") UUID feedbackId);

    // ==================== Statistics Queries ====================

    /**
     * Get total feedback count
     */
    @Query("SELECT COUNT(f) FROM FeedbackModel f")
    long getTotalFeedbacks();

    /**
     * Get feedback count by status
     */
    @Query("SELECT COUNT(f) FROM FeedbackModel f WHERE f.status = :status")
    long countByStatus(@Param("status") FeedbackStatus status);

    /**
     * Get average rating for a user
     */
    @Query("SELECT AVG(f.overallRating) FROM FeedbackModel f WHERE f.receiver.id = :userId")
    Double getAverageRatingForUser(@Param("userId") UUID userId);

    /**
     * Get rating distribution for a user
     */
    @Query("SELECT f.overallRating, COUNT(f) FROM FeedbackModel f " +
           "WHERE f.receiver.id = :userId AND f.isDeleted = false " +
           "GROUP BY f.overallRating")
    List<Object[]> getRatingDistribution(@Param("userId") UUID userId);

    /**
     * Get daily feedback statistics
     */
    @Query("SELECT DATE(f.createdAt), COUNT(f) FROM FeedbackModel f " +
           "WHERE f.createdAt > :since GROUP BY DATE(f.createdAt)")
    List<Object[]> getDailyFeedbackStats(@Param("since") LocalDateTime since);

    /**
     * Get feedback count by purpose
     */
    @Query("SELECT f.purpose, COUNT(f) FROM FeedbackModel f " +
           "WHERE f.receiver.id = :userId GROUP BY f.purpose")
    List<Object[]> getFeedbackCountByPurpose(@Param("userId") UUID userId);

    /**
     * Get anonymous vs named feedback ratio
     */
    @Query("SELECT f.isAnonymous, COUNT(f) FROM FeedbackModel f " +
           "WHERE f.receiver.id = :userId GROUP BY f.isAnonymous")
    List<Object[]> getAnonymousVsNamedRatio(@Param("userId") UUID userId);

    /**
     * Find top rated feedbacks for a user
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :userId " +
           "AND f.status = 'PUBLISHED' AND f.isDeleted = false " +
           "ORDER BY f.overallRating DESC")
    Page<FeedbackModel> findTopRatedFeedbacks(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    /**
     * Find feedbacks by relationship type
     */
    @Query("SELECT f FROM FeedbackModel f WHERE f.receiver.id = :userId " +
           "AND f.relationshipType = :relationshipType AND f.isDeleted = false")
    List<FeedbackModel> findFeedbacksByRelationshipType(
        @Param("userId") UUID userId,
        @Param("relationshipType") String relationshipType
    );
}
