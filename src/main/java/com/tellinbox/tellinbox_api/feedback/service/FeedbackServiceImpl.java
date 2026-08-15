package com.tellinbox.tellinbox_api.feedback.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackRequest;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import com.tellinbox.tellinbox_api.feedback.mapper.FeedbackMapper;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackReportModel;
import com.tellinbox.tellinbox_api.feedback.repository.FeedbackRepository;
import com.tellinbox.tellinbox_api.user.enums.RelationshipType;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for feedback operations.
 * Provides business logic for feedback management.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final MessageSource messageSource;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper feedbackMapper;

    // ==================== Core CRUD Operations ====================

    @Override
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        log.info("Creating new feedback for receiver: {}", request.getReceiverId());

        // Validate receiver exists
        UserModel receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.recipient_user_not_found")));

        // Validate author if not anonymous
        UserModel author = null;
        boolean isAnonymous = Boolean.TRUE.equals(request.getIsAnonymous());
        
        if (!isAnonymous && request.getAuthorId() != null) {
            author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.sender_user_not_found")));
        }

        // Build feedback entity using mapper
        FeedbackModel feedback = feedbackMapper.toEntity(request, author, receiver);

        // Save feedback
        FeedbackModel savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback created successfully with ID: {}", savedFeedback.getId());

        // Update receiver's feedback count
        receiver.incrementFeedbackCount();
        receiver.updateAverageScore(request.getOverallRating() != null ? request.getOverallRating() : 0.0);
        userRepository.save(receiver);

        return feedbackMapper.toDto(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedbackResponse> findById(UUID id) {
        log.debug("Finding feedback by ID: {}", id);
        return feedbackRepository.findById(id).map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> findAll(Pageable pageable) {
        log.debug("Finding all feedbacks with pagination: {}", pageable);
        return feedbackRepository.findAll(pageable).map(feedbackMapper::toDto);
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedback(UUID feedbackId, FeedbackRequest request) {
        log.info("Updating feedback with ID: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));

        // Update fields
        if (request.getTitle() != null) {
            feedback.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            feedback.setContent(request.getContent());
        }
        if (request.getVisibility() != null) {
            feedback.setVisibility(request.getVisibility());
        }
        if (request.getPurpose() != null) {
            feedback.setPurpose(request.getPurpose());
        }
        if (request.getRelationshipType() != null) {
            feedback.setRelationshipType(request.getRelationshipType());
        }
        if (request.getOverallRating() != null) {
            feedback.setOverallRating(request.getOverallRating());
        }

        FeedbackModel updatedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback updated successfully: {}", feedbackId);

        return feedbackMapper.toDto(updatedFeedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(UUID feedbackId) {
        log.info("Soft deleting feedback with ID: {}", feedbackId);
        
        int deleted = feedbackRepository.softDeleteFeedback(feedbackId);
        if (deleted == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found_or_deleted"));
        }
        
        log.info("Feedback soft deleted successfully: {}", feedbackId);
    }

    // ==================== Receiver Operations ====================

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksByReceiver(UUID receiverId, Pageable pageable) {
        log.debug("Finding feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findByReceiverId(receiverId, pageable).map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getPublishedFeedbacksByReceiver(UUID receiverId, Pageable pageable) {
        log.debug("Finding published feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findPublishedByReceiver(receiverId, FeedbackStatus.PUBLISHED, pageable)
            .map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getPendingFeedbacksByReceiver(UUID receiverId) {
        log.debug("Finding pending feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findByReceiverIdAndStatus(receiverId, FeedbackStatus.PENDING)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getUnreadFeedbacksByReceiver(UUID receiverId) {
        log.debug("Finding unread feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findUnreadFeedbacks(receiverId)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(UUID feedbackId) {
        log.info("Marking feedback as read: {}", feedbackId);
        
        int updated = feedbackRepository.markAsRead(feedbackId);
        if (updated == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found"));
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID receiverId) {
        log.info("Marking all feedbacks as read for receiver: {}", receiverId);
        
        List<FeedbackModel> unreadFeedbacks = feedbackRepository.findUnreadFeedbacks(receiverId);
        for (FeedbackModel feedback : unreadFeedbacks) {
            feedback.markAsRead();
        }
        feedbackRepository.saveAll(unreadFeedbacks);
    }

    // ==================== Author Operations ====================

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksByAuthor(UUID authorId, Pageable pageable) {
        log.debug("Finding feedbacks by author: {}", authorId);
        return feedbackRepository.findByAuthorId(authorId, pageable).map(feedbackMapper::toDto);
    }

    // ==================== Status Management ====================

    @Override
    @Transactional
    public FeedbackResponse publishFeedback(UUID feedbackId) {
        log.info("Publishing feedback: {}", feedbackId);
        
        int published = feedbackRepository.publishFeedback(feedbackId);
        if (published == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found"));
        }
        
        return findById(feedbackId).orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));
    }

    @Override
    @Transactional
    public FeedbackResponse archiveFeedback(UUID feedbackId, UUID archivedBy) {
        log.info("Archiving feedback: {} by user: {}", feedbackId, archivedBy);
        
        int archived = feedbackRepository.archiveFeedback(feedbackId, archivedBy);
        if (archived == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found"));
        }
        
        return findById(feedbackId).orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedbackStatus(UUID feedbackId, FeedbackStatus status) {
        log.info("Updating feedback status: {} to {}", feedbackId, status);
        
        int updated = feedbackRepository.updateStatus(feedbackId, status);
        if (updated == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found"));
        }
        
        return findById(feedbackId).orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedbackVisibility(UUID feedbackId, FeedbackVisibility visibility) {
        log.info("Updating feedback visibility: {} to {}", feedbackId, visibility);
        
        int updated = feedbackRepository.updateVisibility(feedbackId, visibility);
        if (updated == 0) {
            throw new  TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found"));
        }
        
        return findById(feedbackId).orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));
    }

    // ==================== Response Management ====================

    @Override
    @Transactional
    public FeedbackResponse addResponseToFeedback(UUID feedbackId, String responseContent, Boolean isPublic) {
        log.info("Adding response to feedback: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));

        if (!feedback.canRespond()) {
            throw new TellInboxCustomException.ValidationException(getMessage("error.ValidationException.feedback_already_answered"));
        }

        // Validate response content
        if (responseContent == null || responseContent.trim().isEmpty()) {
            throw new TellInboxCustomException.ValidationException(getMessage("error.ValidationException.response_content_cannot_be_empty"));
        }

        // Create response
        FeedbackResponse responseEntity = FeedbackResponse.builder()
            .feedback(feedback)
            .response(responseContent)
            .isPublic(isPublic != null ? isPublic : false)
            .build();

        feedback.addResponse(responseEntity);
        feedbackRepository.save(feedback);

        log.info("Response added to feedback: {}", feedbackId);
        return feedbackMapper.toDto(feedback);
    }

    @Override
    @Transactional
    public FeedbackResponse updateResponse(UUID feedbackId, String responseContent) {
        log.info("Updating response for feedback: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));

        if (feedback.getResponse() == null) {
            throw new TellInboxCustomException.ValidationException(getMessage("error.IllegalStateException.no_reply_for_this_feedback"));
        }

        feedback.getResponse().updateResponse(responseContent);
        feedbackRepository.save(feedback);

        log.info("Response updated for feedback: {}", feedbackId);
        return feedbackMapper.toDto(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksWithResponses(UUID receiverId, Pageable pageable) {
        log.debug("Finding feedbacks with responses for receiver: {}", receiverId);
        return feedbackRepository.findFeedbacksWithResponses(receiverId, pageable)
            .map(feedbackMapper::toDto);
    }

    // ==================== Reporting & Moderation ====================

    @Override
    @Transactional
    public void reportFeedback(UUID feedbackId, String reason, UUID reportedBy) {
        log.info("Reporting feedback: {} by user: {}", feedbackId, reportedBy);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.feedback_not_found")));

        UserModel reporter = userRepository.findById(reportedBy)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.reporter_user_not_found")));

        // Create report
        FeedbackReportModel report = FeedbackReportModel.builder()
            .feedback(feedback)
            .reporter(reporter)
            .reason(reason)
            .status("PENDING")
            .build();

        feedback.addReport(report);
        feedbackRepository.save(feedback);

        log.info("Feedback reported successfully: {}", feedbackId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFlaggedFeedbacks() {
        log.debug("Finding flagged feedbacks");
        return feedbackRepository.findByIsFlaggedTrue()
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksWithReports(int minReports) {
        log.debug("Finding feedbacks with minimum {} reports", minReports);
        return feedbackRepository.findByReportCountGreaterThan(minReports)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    // ==================== Search & Advanced Queries ====================

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> searchFeedbacks(UUID receiverId, String query, Pageable pageable) {
        log.debug("Searching feedbacks for receiver: {} with query: {}", receiverId, query);
        return feedbackRepository.searchFeedbacks(receiverId, query, pageable)
            .map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getRecentActiveFeedbacks(UUID receiverId, Pageable pageable) {
        log.debug("Finding recent active feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findRecentActiveFeedbacks(receiverId, pageable)
            .map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getTopRatedFeedbacks(UUID receiverId, Pageable pageable) {
        log.debug("Finding top rated feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findTopRatedFeedbacks(receiverId, pageable)
            .map(feedbackMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAnonymousFeedbacks(UUID receiverId) {
        log.debug("Finding anonymous feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findAnonymousFeedbacks(receiverId)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByRelationshipType(UUID receiverId, RelationshipType relationshipType) {
        log.debug("Finding feedbacks by relationship type: {} for receiver: {}", relationshipType, receiverId);
        return feedbackRepository.findFeedbacksByRelationshipType(receiverId, relationshipType)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    // ==================== Statistics & Analytics ====================

    @Override
    @Transactional(readOnly = true)
    public long getTotalFeedbacks() {
        return feedbackRepository.getTotalFeedbacks();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(FeedbackStatus status) {
        return feedbackRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByReceiver(UUID receiverId) {
        return feedbackRepository.countByReceiverId(receiverId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForUser(UUID userId) {
        return feedbackRepository.getAverageRatingForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getRatingDistribution(UUID userId) {
        return feedbackRepository.getRatingDistribution(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyFeedbackStats(LocalDateTime since) {
        return feedbackRepository.getDailyFeedbackStats(since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getFeedbackCountByPurpose(UUID userId) {
        return feedbackRepository.getFeedbackCountByPurpose(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAnonymousVsNamedRatio(UUID userId) {
        return feedbackRepository.getAnonymousVsNamedRatio(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> findFeedbacksInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding feedbacks in date range: {} to {}", startDate, endDate);
        return feedbackRepository.findFeedbacksInDateRange(startDate, endDate)
            .stream()
            .map(feedbackMapper::toDto)
            .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    /**
     * Convert FeedbackModel to FeedbackResponse DTO
     * @deprecated Use FeedbackMapper.toDto() instead
     */
    @Deprecated
    private FeedbackResponse convertToDto(FeedbackModel feedback) {
        return feedbackMapper.toDto(feedback);
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
