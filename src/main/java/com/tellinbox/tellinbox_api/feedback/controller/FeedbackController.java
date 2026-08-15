package com.tellinbox.tellinbox_api.feedback.controller;

import com.tellinbox.tellinbox_api.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackRequest;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import com.tellinbox.tellinbox_api.feedback.service.FeedbackService;
import com.tellinbox.tellinbox_api.security.CustomUserDetails;
import com.tellinbox.tellinbox_api.user.enums.RelationshipType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for feedback operations.
 * Handles CRUD operations, status management, responses, and analytics for feedbacks.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final MessageSource messageSource;
    private final FeedbackService feedbackService;

    // ==================== Core CRUD Operations ====================

    /**
     * Create a new feedback.
     * 
     * @param request feedback creation request
     * @return created feedback response
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID authorId = getCurrentUserId(userDetails);
        FeedbackRequest requestWithAuthor = FeedbackRequest.builder()
                .receiverId(request.getReceiverId())
                .authorId(authorId)
                .isAnonymous(request.getIsAnonymous())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .visibility(request.getVisibility())
                .purpose(request.getPurpose())
                .relationshipType(request.getRelationshipType())
                .overallRating(request.getOverallRating())
                .feedbackRequestId(request.getFeedbackRequestId())
                .build();
        
        FeedbackResponse response = feedbackService.createFeedback(requestWithAuthor);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get feedback by ID.
     * 
     * @param id feedback UUID
     * @return feedback response if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable UUID id) {
        return feedbackService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all feedbacks with pagination.
     * 
     * @param pageable pagination information
     * @return page of feedback responses
     */
    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> getAllFeedbacks(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(feedbackService.findAll(pageable));
    }

    /**
     * Update an existing feedback.
     * 
     * @param feedbackId feedback ID to update
     * @param request feedback update request
     * @return updated feedback response
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable("id") UUID feedbackId,
            @Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse response = feedbackService.updateFeedback(feedbackId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a feedback (soft delete).
     * 
     * @param feedbackId feedback ID to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable("id") UUID feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Receiver Operations ====================

    /**
     * Get feedbacks received by the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of feedback responses
     */
    @GetMapping("/received")
    public ResponseEntity<Page<FeedbackResponse>> getMyReceivedFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getFeedbacksByReceiver(receiverId, pageable));
    }

    /**
     * Get published feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of published feedback responses
     */
    @GetMapping("/received/published")
    public ResponseEntity<Page<FeedbackResponse>> getMyPublishedFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getPublishedFeedbacksByReceiver(receiverId, pageable));
    }

    /**
     * Get pending feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @return list of pending feedback responses
     */
    @GetMapping("/received/pending")
    public ResponseEntity<List<FeedbackResponse>> getMyPendingFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getPendingFeedbacksByReceiver(receiverId));
    }

    /**
     * Get unread feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @return list of unread feedback responses
     */
    @GetMapping("/received/unread")
    public ResponseEntity<List<FeedbackResponse>> getMyUnreadFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getUnreadFeedbacksByReceiver(receiverId));
    }

    /**
     * Mark a feedback as read.
     * 
     * @param feedbackId feedback ID
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markFeedbackAsRead(@PathVariable("id") UUID feedbackId) {
        feedbackService.markAsRead(feedbackId);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all feedbacks as read for the current user.
     * 
     * @param userDetails authenticated user details
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllFeedbacksAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        feedbackService.markAllAsRead(receiverId);
        return ResponseEntity.ok().build();
    }

    // ==================== Author Operations ====================

    /**
     * Get feedbacks given by the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of feedback responses
     */
    @GetMapping("/given")
    public ResponseEntity<Page<FeedbackResponse>> getMyGivenFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID authorId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getFeedbacksByAuthor(authorId, pageable));
    }

    // ==================== Status Management ====================

    /**
     * Publish a feedback.
     * 
     * @param feedbackId feedback ID
     * @return updated feedback response
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<FeedbackResponse> publishFeedback(@PathVariable("id") UUID feedbackId) {
        FeedbackResponse response = feedbackService.publishFeedback(feedbackId);
        return ResponseEntity.ok(response);
    }

    /**
     * Archive a feedback.
     * 
     * @param feedbackId feedback ID
     * @param userDetails authenticated user details
     * @return updated feedback response
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<FeedbackResponse> archiveFeedback(
            @PathVariable("id") UUID feedbackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID archivedBy = getCurrentUserId(userDetails);
        FeedbackResponse response = feedbackService.archiveFeedback(feedbackId, archivedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Update feedback status.
     * 
     * @param feedbackId feedback ID
     * @param status new status
     * @return updated feedback response
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<FeedbackResponse> updateFeedbackStatus(
            @PathVariable("id") UUID feedbackId,
            @RequestParam FeedbackStatus status) {
        FeedbackResponse response = feedbackService.updateFeedbackStatus(feedbackId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Update feedback visibility.
     * 
     * @param feedbackId feedback ID
     * @param visibility new visibility
     * @return updated feedback response
     */
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<FeedbackResponse> updateFeedbackVisibility(
            @PathVariable("id") UUID feedbackId,
            @RequestParam FeedbackVisibility visibility) {
        FeedbackResponse response = feedbackService.updateFeedbackVisibility(feedbackId, visibility);
        return ResponseEntity.ok(response);
    }

    // ==================== Response Management ====================

    /**
     * Add response to a feedback.
     * 
     * @param feedbackId feedback ID
     * @param requestBody contains response content and isPublic flag
     * @return updated feedback response
     */
    @PostMapping("/{id}/response")
    public ResponseEntity<FeedbackResponse> addResponseToFeedback(
            @PathVariable("id") UUID feedbackId,
            @RequestBody Map<String, Object> requestBody) {
        
        String responseContent = (String) requestBody.get("responseContent");
        Boolean isPublic = (Boolean) requestBody.getOrDefault("isPublic", false);
        
        FeedbackResponse response = feedbackService.addResponseToFeedback(feedbackId, responseContent, isPublic);
        return ResponseEntity.ok(response);
    }

    /**
     * Update response to a feedback.
     * 
     * @param feedbackId feedback ID
     * @param requestBody contains new response content
     * @return updated feedback response
     */
    @PutMapping("/{id}/response")
    public ResponseEntity<FeedbackResponse> updateResponse(
            @PathVariable("id") UUID feedbackId,
            @RequestBody Map<String, String> requestBody) {
        
        String responseContent = requestBody.get("responseContent");
        FeedbackResponse response = feedbackService.updateResponse(feedbackId, responseContent);
        return ResponseEntity.ok(response);
    }

    /**
     * Get feedbacks with responses for the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of feedback responses with responses
     */
    @GetMapping("/with-responses")
    public ResponseEntity<Page<FeedbackResponse>> getMyFeedbacksWithResponses(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getFeedbacksWithResponses(receiverId, pageable));
    }

    // ==================== Reporting & Moderation ====================

    /**
     * Report a feedback.
     * 
     * @param feedbackId feedback ID
     * @param requestBody contains report reason
     * @param userDetails authenticated user details
     */
    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportFeedback(
            @PathVariable("id") UUID feedbackId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String reason = requestBody.get("reason");
        UUID reportedBy = getCurrentUserId(userDetails);
        feedbackService.reportFeedback(feedbackId, reason, reportedBy);
        return ResponseEntity.ok().build();
    }

    /**
     * Get flagged feedbacks (admin operation).
     * 
     * @return list of flagged feedback responses
     */
    @GetMapping("/flagged")
    public ResponseEntity<List<FeedbackResponse>> getFlaggedFeedbacks() {
        return ResponseEntity.ok(feedbackService.getFlaggedFeedbacks());
    }

    /**
     * Get feedbacks with minimum reports (admin operation).
     * 
     * @param minReports minimum number of reports
     * @return list of feedback responses
     */
    @GetMapping("/reported")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksWithReports(
            @RequestParam(defaultValue = "3") int minReports) {
        return ResponseEntity.ok(feedbackService.getFeedbacksWithReports(minReports));
    }

    // ==================== Search & Advanced Queries ====================

    /**
     * Search feedbacks by content.
     * 
     * @param query search query
     * @param pageable pagination information
     * @param userDetails authenticated user details
     * @return page of matching feedback responses
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FeedbackResponse>> searchFeedbacks(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.searchFeedbacks(receiverId, query, pageable));
    }

    /**
     * Get recent active feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of active feedback responses
     */
    @GetMapping("/recent-active")
    public ResponseEntity<Page<FeedbackResponse>> getRecentActiveFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getRecentActiveFeedbacks(receiverId, pageable));
    }

    /**
     * Get top rated feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @param pageable pagination information
     * @return page of top rated feedback responses
     */
    @GetMapping("/top-rated")
    public ResponseEntity<Page<FeedbackResponse>> getTopRatedFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getTopRatedFeedbacks(receiverId, pageable));
    }

    /**
     * Get anonymous feedbacks for the current user.
     * 
     * @param userDetails authenticated user details
     * @return list of anonymous feedback responses
     */
    @GetMapping("/anonymous")
    public ResponseEntity<List<FeedbackResponse>> getMyAnonymousFeedbacks(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getAnonymousFeedbacks(receiverId));
    }

    /**
     * Get feedbacks by relationship type.
     * 
     * @param relationshipType relationship type
     * @param userDetails authenticated user details
     * @return list of feedback responses
     */
    @GetMapping("/by-relationship")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByRelationshipType(
            @RequestParam RelationshipType relationshipType,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getFeedbacksByRelationshipType(receiverId, relationshipType));
    }

    // ==================== Statistics & Analytics ====================

    /**
     * Get total feedback count.
     * 
     * @return total feedback count
     */
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalFeedbacks() {
        return ResponseEntity.ok(feedbackService.getTotalFeedbacks());
    }

    /**
     * Get feedback count by status.
     * 
     * @param status feedback status
     * @return count of feedbacks with given status
     */
    @GetMapping("/stats/by-status")
    public ResponseEntity<Long> countByStatus(@RequestParam FeedbackStatus status) {
        return ResponseEntity.ok(feedbackService.countByStatus(status));
    }

    /**
     * Get feedback count for the current user.
     * 
     * @param userDetails authenticated user details
     * @return feedback count
     */
    @GetMapping("/stats/my-count")
    public ResponseEntity<Long> getMyFeedbackCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID receiverId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(feedbackService.countByReceiver(receiverId));
    }

    /**
     * Get average rating for a user.
     * 
     * @param userId user ID
     * @return average rating
     */
    @GetMapping("/stats/average-rating/{userId}")
    public ResponseEntity<Double> getAverageRatingForUser(@PathVariable UUID userId) {
        Double averageRating = feedbackService.getAverageRatingForUser(userId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    /**
     * Get rating distribution for a user.
     * 
     * @param userId user ID
     * @return list of rating-count pairs
     */
    @GetMapping("/stats/rating-distribution/{userId}")
    public ResponseEntity<List<Object[]>> getRatingDistribution(@PathVariable UUID userId) {
        return ResponseEntity.ok(feedbackService.getRatingDistribution(userId));
    }

    /**
     * Get daily feedback statistics.
     * 
     * @param since start date (optional, defaults to 30 days ago)
     * @return list of date-count pairs
     */
    @GetMapping("/stats/daily")
    public ResponseEntity<List<Object[]>> getDailyFeedbackStats(
            @RequestParam(required = false) LocalDateTime since) {
        
        if (since == null) {
            since = LocalDateTime.now().minusDays(30);
        }
        return ResponseEntity.ok(feedbackService.getDailyFeedbackStats(since));
    }

    /**
     * Get feedback count by purpose for a user.
     * 
     * @param userId user ID
     * @return list of purpose-count pairs
     */
    @GetMapping("/stats/by-purpose/{userId}")
    public ResponseEntity<List<Object[]>> getFeedbackCountByPurpose(@PathVariable UUID userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackCountByPurpose(userId));
    }

    /**
     * Get anonymous vs named feedback ratio for a user.
     * 
     * @param userId user ID
     * @return list of isAnonymous-count pairs
     */
    @GetMapping("/stats/anonymity-ratio/{userId}")
    public ResponseEntity<List<Object[]>> getAnonymousVsNamedRatio(@PathVariable UUID userId) {
        return ResponseEntity.ok(feedbackService.getAnonymousVsNamedRatio(userId));
    }

    /**
     * Get feedbacks in date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of feedback responses
     */
    @GetMapping("/stats/date-range")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksInDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(feedbackService.findFeedbacksInDateRange(startDate, endDate));
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to extract user ID from authenticated user details.
     */
    private UUID getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }
        throw new TellInboxCustomException.ApplicationServerException(getMessage("error.IllegalStateException.unable_to_extract_user_id_from_authentication_context"));
    }

    /**
     * Get localized message from messages.properties
     * @param key Message key
     * @param args Optional arguments for message formatting
     * @return Localized message
     */
    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
