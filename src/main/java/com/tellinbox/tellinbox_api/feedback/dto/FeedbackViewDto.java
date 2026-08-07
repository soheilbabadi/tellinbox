package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Feedback response.
 * Used for API response handling.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackViewDto {

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
}
