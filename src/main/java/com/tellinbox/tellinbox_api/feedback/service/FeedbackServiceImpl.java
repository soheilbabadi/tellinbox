package com.tellinbox.tellinbox_api.feedback.service;

import com.tellinbox.tellinbox_api.feedback.dto.FeedbackRequest;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import com.tellinbox.tellinbox_api.feedback.model.*;
import com.tellinbox.tellinbox_api.feedback.repository.FeedbackRepository;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    // ==================== Core CRUD Operations ====================

    @Override
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        log.info("Creating new feedback for receiver: {}", request.getReceiverId());

        // Validate receiver exists
        UserModel receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new IllegalArgumentException("کاربر دریافت‌کننده یافت نشد"));

        // Validate author if not anonymous
        UserModel author = null;
        boolean isAnonymous = Boolean.TRUE.equals(request.getIsAnonymous());
        
        if (!isAnonymous && request.getAuthorId() != null) {
            author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("کاربر ارسال‌کننده یافت نشد"));
        }

        // Build feedback entity
        FeedbackModel feedback = FeedbackModel.builder()
            .receiver(receiver)
            .author(author)
            .isAnonymous(isAnonymous)
            .title(request.getTitle())
            .content(request.getContent())
            .status(FeedbackStatus.PENDING)
            .visibility(request.getVisibility() != null ? request.getVisibility() : FeedbackVisibility.PRIVATE)
            .purpose(request.getPurpose())
            .relationshipType(request.getRelationshipType())
            .overallRating(0.0)
            .isRead(false)
            .hasResponse(false)
            .isFlagged(false)
            .reportCount(0)
            .feedbackRequestId(request.getFeedbackRequestId())
            .build();

        // Save feedback
        FeedbackModel savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback created successfully with ID: {}", savedFeedback.getId());

        // Update receiver's feedback count
        receiver.incrementFeedbackCount();
        receiver.updateAverageScore(request.getOverallRating() != null ? request.getOverallRating() : 0.0);
        userRepository.save(receiver);

        return convertToDto(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedbackResponse> findById(UUID id) {
        log.debug("Finding feedback by ID: {}", id);
        return feedbackRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> findAll(Pageable pageable) {
        log.debug("Finding all feedbacks with pagination: {}", pageable);
        return feedbackRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedback(UUID feedbackId, FeedbackRequest request) {
        log.info("Updating feedback with ID: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));

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

        return convertToDto(updatedFeedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(UUID feedbackId) {
        log.info("Soft deleting feedback with ID: {}", feedbackId);
        
        int deleted = feedbackRepository.softDeleteFeedback(feedbackId);
        if (deleted == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد یا قبلاً حذف شده است");
        }
        
        log.info("Feedback soft deleted successfully: {}", feedbackId);
    }

    // ==================== Receiver Operations ====================

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksByReceiver(UUID receiverId, Pageable pageable) {
        log.debug("Finding feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findByReceiverId(receiverId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getPublishedFeedbacksByReceiver(UUID receiverId, Pageable pageable) {
        log.debug("Finding published feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findPublishedByReceiver(receiverId, FeedbackStatus.PUBLISHED, pageable)
            .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getPendingFeedbacksByReceiver(UUID receiverId) {
        log.debug("Finding pending feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findByReceiverIdAndStatus(receiverId, FeedbackStatus.PENDING)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getUnreadFeedbacksByReceiver(UUID receiverId) {
        log.debug("Finding unread feedbacks by receiver: {}", receiverId);
        return feedbackRepository.findUnreadFeedbacks(receiverId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(UUID feedbackId) {
        log.info("Marking feedback as read: {}", feedbackId);
        
        int updated = feedbackRepository.markAsRead(feedbackId);
        if (updated == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد");
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
        return feedbackRepository.findByAuthorId(authorId, pageable).map(this::convertToDto);
    }

    // ==================== Status Management ====================

    @Override
    @Transactional
    public FeedbackResponse publishFeedback(UUID feedbackId) {
        log.info("Publishing feedback: {}", feedbackId);
        
        int published = feedbackRepository.publishFeedback(feedbackId);
        if (published == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد");
        }
        
        return findById(feedbackId).orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));
    }

    @Override
    @Transactional
    public FeedbackResponse archiveFeedback(UUID feedbackId, UUID archivedBy) {
        log.info("Archiving feedback: {} by user: {}", feedbackId, archivedBy);
        
        int archived = feedbackRepository.archiveFeedback(feedbackId, archivedBy);
        if (archived == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد");
        }
        
        return findById(feedbackId).orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedbackStatus(UUID feedbackId, FeedbackStatus status) {
        log.info("Updating feedback status: {} to {}", feedbackId, status);
        
        int updated = feedbackRepository.updateStatus(feedbackId, status);
        if (updated == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد");
        }
        
        return findById(feedbackId).orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));
    }

    @Override
    @Transactional
    public FeedbackResponse updateFeedbackVisibility(UUID feedbackId, FeedbackVisibility visibility) {
        log.info("Updating feedback visibility: {} to {}", feedbackId, visibility);
        
        int updated = feedbackRepository.updateVisibility(feedbackId, visibility);
        if (updated == 0) {
            throw new IllegalArgumentException("بازخورد یافت نشد");
        }
        
        return findById(feedbackId).orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));
    }

    // ==================== Response Management ====================

    @Override
    @Transactional
    public FeedbackResponse addResponseToFeedback(UUID feedbackId, String responseContent, Boolean isPublic) {
        log.info("Adding response to feedback: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));

        if (!feedback.canRespond()) {
            throw new IllegalStateException("این بازخورد نمی‌تواند پاسخ داده شود");
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
        return convertToDto(feedback);
    }

    @Override
    @Transactional
    public FeedbackResponse updateResponse(UUID feedbackId, String responseContent) {
        log.info("Updating response for feedback: {}", feedbackId);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));

        if (feedback.getResponse() == null) {
            throw new IllegalStateException("پاسخی برای این بازخورد وجود ندارد");
        }

        feedback.getResponse().updateResponse(responseContent);
        feedbackRepository.save(feedback);

        log.info("Response updated for feedback: {}", feedbackId);
        return convertToDto(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksWithResponses(UUID receiverId, Pageable pageable) {
        log.debug("Finding feedbacks with responses for receiver: {}", receiverId);
        return feedbackRepository.findFeedbacksWithResponses(receiverId, pageable)
            .map(this::convertToDto);
    }

    // ==================== Reporting & Moderation ====================

    @Override
    @Transactional
    public void reportFeedback(UUID feedbackId, String reason, UUID reportedBy) {
        log.info("Reporting feedback: {} by user: {}", feedbackId, reportedBy);

        FeedbackModel feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("بازخورد یافت نشد"));

        UserModel reporter = userRepository.findById(reportedBy)
            .orElseThrow(() -> new IllegalArgumentException("کاربر گزارش‌دهنده یافت نشد"));

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
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksWithReports(int minReports) {
        log.debug("Finding feedbacks with minimum {} reports", minReports);
        return feedbackRepository.findByReportCountGreaterThan(minReports)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ==================== Search & Advanced Queries ====================

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> searchFeedbacks(UUID receiverId, String query, Pageable pageable) {
        log.debug("Searching feedbacks for receiver: {} with query: {}", receiverId, query);
        return feedbackRepository.searchFeedbacks(receiverId, query, pageable)
            .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getRecentActiveFeedbacks(UUID receiverId, Pageable pageable) {
        log.debug("Finding recent active feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findRecentActiveFeedbacks(receiverId, pageable)
            .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getTopRatedFeedbacks(UUID receiverId, Pageable pageable) {
        log.debug("Finding top rated feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findTopRatedFeedbacks(receiverId, pageable)
            .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAnonymousFeedbacks(UUID receiverId) {
        log.debug("Finding anonymous feedbacks for receiver: {}", receiverId);
        return feedbackRepository.findAnonymousFeedbacks(receiverId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByRelationshipType(UUID receiverId, String relationshipType) {
        log.debug("Finding feedbacks by relationship type: {} for receiver: {}", relationshipType, receiverId);
        return feedbackRepository.findFeedbacksByRelationshipType(receiverId, relationshipType)
            .stream()
            .map(this::convertToDto)
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
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    /**
     * Convert FeedbackModel to FeedbackResponse DTO
     */
    private FeedbackResponse convertToDto(FeedbackModel feedback) {
        if (feedback == null) {
            return null;
        }

        return FeedbackResponse.builder()
            .id(feedback.getId())
            .receiverId(feedback.getReceiver().getId())
            .receiverName(feedback.getReceiver().getDisplayName())
            .authorId(feedback.getAuthor() != null ? feedback.getAuthor().getId() : null)
            .authorName(feedback.getAuthorDisplayName())
            .isAnonymous(feedback.isAnonymous())
            .title(feedback.getTitle())
            .content(feedback.getContent())
            .status(feedback.getStatus() != null ? feedback.getStatus().name() : null)
            .visibility(feedback.getVisibility() != null ? feedback.getVisibility().name() : null)
            .purpose(feedback.getPurpose() != null ? feedback.getPurpose().name() : null)
            .relationshipType(feedback.getRelationshipType())
            .overallRating(feedback.getOverallRating())
            .isRead(feedback.getIsRead())
            .readAt(feedback.getReadAt())
            .hasResponse(feedback.getHasResponse())
            .isFlagged(feedback.getIsFlagged())
            .reportCount(feedback.getReportCount())
            .feedbackRequestId(feedback.getFeedbackRequestId())
            .createdAt(feedback.getCreatedAt())
            .updatedAt(feedback.getUpdatedAt())
            .publishedAt(feedback.getPublishedAt())
            .archivedAt(feedback.getArchivedAt())
            .responseText(feedback.getResponse() != null ? feedback.getResponse().getResponse() : null)
            .isResponsePublic(feedback.getResponse() != null ? feedback.getResponse().getIsPublic() : null)
            .build();
    }
}
