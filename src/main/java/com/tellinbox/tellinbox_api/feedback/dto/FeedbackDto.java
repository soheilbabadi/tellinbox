package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Feedback operations.
 * Used for API request/response handling.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {

    private UUID id;
    private UUID receiverId;
    private String receiverName;
    private UUID authorId;
    private String authorName;
    private Boolean isAnonymous;
    private String title;
    private String content;
    private FeedbackStatus status;
    private FeedbackVisibility visibility;
    private FeedbackPurpose purpose;
    private String relationshipType;
    private Double overallRating;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean hasResponse;
    private Boolean isFlagged;
    private Integer reportCount;
    private UUID feedbackRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;
    private String responseText;
    private Boolean isResponsePublic;
    
    /**
     * List of category scores for detailed feedback evaluation
     */
    private List<CategoryScoreDto> categoryScores;
}
