package com.tellinbox.tellinbox_api.feedback.service;

import com.tellinbox.tellinbox_api.feedback.dto.FeedbackRequest;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for feedback operations.
 * Defines the contract for feedback management functionality.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
public interface FeedbackService {

    // ==================== Core CRUD Operations ====================

    /**
     * Create a new feedback
     * @param request Feedback creation request
     * @return Created feedback DTO
     */
    FeedbackResponse createFeedback(FeedbackRequest request);

    /**
     * Find feedback by ID
     * @param id Feedback UUID
     * @return Optional containing feedback if found
     */
    Optional<FeedbackResponse> findById(UUID id);

    /**
     * Get all feedbacks with pagination
     * @param pageable Pagination information
     * @return Page of feedback DTOs
     */
    Page<FeedbackResponse> findAll(Pageable pageable);

    /**
     * Update feedback
     * @param feedbackId Feedback ID
     * @param request Feedback update request
     * @return Updated feedback DTO
     */
    FeedbackResponse updateFeedback(UUID feedbackId, FeedbackRequest request);

    /**
     * Delete feedback (soft delete)
     * @param feedbackId Feedback ID to delete
     */
    void deleteFeedback(UUID feedbackId);

    // ==================== Receiver Operations ====================

    /**
     * Get feedbacks received by a user
     * @param receiverId Receiver user ID
     * @param pageable Pagination information
     * @return Page of feedback DTOs
     */
    Page<FeedbackResponse> getFeedbacksByReceiver(UUID receiverId, Pageable pageable);

    /**
     * Get published feedbacks for a receiver
     * @param receiverId Receiver user ID
     * @param pageable Pagination information
     * @return Page of published feedback DTOs
     */
    Page<FeedbackResponse> getPublishedFeedbacksByReceiver(UUID receiverId, Pageable pageable);

    /**
     * Get pending feedbacks for a receiver
     * @param receiverId Receiver user ID
     * @return List of pending feedback DTOs
     */
    List<FeedbackResponse> getPendingFeedbacksByReceiver(UUID receiverId);

    /**
     * Get unread feedbacks for a receiver
     * @param receiverId Receiver user ID
     * @return List of unread feedback DTOs
     */
    List<FeedbackResponse> getUnreadFeedbacksByReceiver(UUID receiverId);

    /**
     * Mark feedback as read
     * @param feedbackId Feedback ID
     */
    void markAsRead(UUID feedbackId);

    /**
     * Mark all feedbacks as read for a user
     * @param receiverId Receiver user ID
     */
    void markAllAsRead(UUID receiverId);

    // ==================== Author Operations ====================

    /**
     * Get feedbacks given by a user
     * @param authorId Author user ID
     * @param pageable Pagination information
     * @return Page of feedback DTOs
     */
    Page<FeedbackResponse> getFeedbacksByAuthor(UUID authorId, Pageable pageable);

    // ==================== Status Management ====================

    /**
     * Publish a feedback
     * @param feedbackId Feedback ID
     * @return Updated feedback DTO
     */
    FeedbackResponse publishFeedback(UUID feedbackId);

    /**
     * Archive a feedback
     * @param feedbackId Feedback ID
     * @param archivedBy User ID who is archiving
     * @return Updated feedback DTO
     */
    FeedbackResponse archiveFeedback(UUID feedbackId, UUID archivedBy);

    /**
     * Update feedback status
     * @param feedbackId Feedback ID
     * @param status New status
     * @return Updated feedback DTO
     */
    FeedbackResponse updateFeedbackStatus(UUID feedbackId, FeedbackStatus status);

    /**
     * Update feedback visibility
     * @param feedbackId Feedback ID
     * @param visibility New visibility
     * @return Updated feedback DTO
     */
    FeedbackResponse updateFeedbackVisibility(UUID feedbackId, FeedbackVisibility visibility);

    // ==================== Response Management ====================

    /**
     * Add response to a feedback
     * @param feedbackId Feedback ID
     * @param responseContent Response content
     * @param isPublic Whether response is public
     * @return Updated feedback DTO
     */
    FeedbackResponse addResponseToFeedback(UUID feedbackId, String responseContent, Boolean isPublic);

    /**
     * Update response to a feedback
     * @param feedbackId Feedback ID
     * @param responseContent New response content
     * @return Updated feedback DTO
     */
    FeedbackResponse updateResponse(UUID feedbackId, String responseContent);

    /**
     * Get feedbacks with responses
     * @param receiverId Receiver user ID
     * @param pageable Pagination information
     * @return Page of feedback DTOs with responses
     */
    Page<FeedbackResponse> getFeedbacksWithResponses(UUID receiverId, Pageable pageable);

    // ==================== Reporting & Moderation ====================

    /**
     * Report a feedback
     * @param feedbackId Feedback ID
     * @param reason Report reason
     * @param reportedBy User ID who is reporting
     */
    void reportFeedback(UUID feedbackId, String reason, UUID reportedBy);

    /**
     * Get flagged feedbacks
     * @return List of flagged feedback DTOs
     */
    List<FeedbackResponse> getFlaggedFeedbacks();

    /**
     * Get feedbacks with minimum reports
     * @param minReports Minimum number of reports
     * @return List of feedback DTOs
     */
    List<FeedbackResponse> getFeedbacksWithReports(int minReports);

    // ==================== Search & Advanced Queries ====================

    /**
     * Search feedbacks by content
     * @param receiverId Receiver user ID
     * @param query Search query
     * @param pageable Pagination information
     * @return Page of matching feedback DTOs
     */
    Page<FeedbackResponse> searchFeedbacks(UUID receiverId, String query, Pageable pageable);

    /**
     * Get recent active feedbacks
     * @param receiverId Receiver user ID
     * @param pageable Pagination information
     * @return Page of active feedback DTOs
     */
    Page<FeedbackResponse> getRecentActiveFeedbacks(UUID receiverId, Pageable pageable);

    /**
     * Get top rated feedbacks
     * @param receiverId Receiver user ID
     * @param pageable Pagination information
     * @return Page of top rated feedback DTOs
     */
    Page<FeedbackResponse> getTopRatedFeedbacks(UUID receiverId, Pageable pageable);

    /**
     * Get anonymous feedbacks
     * @param receiverId Receiver user ID
     * @return List of anonymous feedback DTOs
     */
    List<FeedbackResponse> getAnonymousFeedbacks(UUID receiverId);

    /**
     * Get feedbacks by relationship type
     * @param receiverId Receiver user ID
     * @param relationshipType Relationship type
     * @return List of feedback DTOs
     */
    List<FeedbackResponse> getFeedbacksByRelationshipType(UUID receiverId, String relationshipType);

    // ==================== Statistics & Analytics ====================

    /**
     * Get total feedback count
     * @return Total feedback count
     */
    long getTotalFeedbacks();

    /**
     * Get feedback count by status
     * @param status Feedback status
     * @return Count of feedbacks with given status
     */
    long countByStatus(FeedbackStatus status);

    /**
     * Get feedback count for a receiver
     * @param receiverId Receiver user ID
     * @return Feedback count
     */
    long countByReceiver(UUID receiverId);

    /**
     * Get average rating for a user
     * @param userId User ID
     * @return Average rating
     */
    Double getAverageRatingForUser(UUID userId);

    /**
     * Get rating distribution for a user
     * @param userId User ID
     * @return List of rating-count pairs
     */
    List<Object[]> getRatingDistribution(UUID userId);

    /**
     * Get daily feedback statistics
     * @param since Start date
     * @return List of date-count pairs
     */
    List<Object[]> getDailyFeedbackStats(LocalDateTime since);

    /**
     * Get feedback count by purpose
     * @param userId User ID
     * @return List of purpose-count pairs
     */
    List<Object[]> getFeedbackCountByPurpose(UUID userId);

    /**
     * Get anonymous vs named feedback ratio
     * @param userId User ID
     * @return List of isAnonymous-count pairs
     */
    List<Object[]> getAnonymousVsNamedRatio(UUID userId);

    /**
     * Find feedbacks in date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of feedback DTOs
     */
    List<FeedbackResponse> findFeedbacksInDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
