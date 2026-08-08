package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a response to a feedback.
 * The receiver can respond to feedbacks they've received.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime deletedAt;
    private Boolean isDeleted = false;

    /**
     * ID of the receiver
     */
    private UUID receiverId;

    /**
     * Name of the receiver
     */
    private String receiverName;

    /**
     * ID of the author
     */
    private UUID authorId;

    /**
     * Name of the author
     */
    private String authorName;

    /**
     * Whether the feedback is anonymous
     */
    private Boolean isAnonymous;

    /**
     * Title of the feedback
     */
    private String title;

    /**
     * Content of the feedback
     */
    private String content;

    /**
     * Status of the feedback
     */
    private String status;

    /**
     * Visibility of the feedback
     */
    private String visibility;

    /**
     * Purpose of the feedback
     */
    private String purpose;

    /**
     * Relationship type
     */
    private String relationshipType;

    /**
     * Overall rating
     */
    private Double overallRating;

    /**
     * Whether the feedback is read
     */
    private Boolean isRead;

    /**
     * When the feedback was read
     */
    private LocalDateTime readAt;

    /**
     * When the feedback was submitted
     */
    private LocalDateTime submittedAt;

    /**
     * Category of the feedback
     */
    private String category;

    /**
     * Tags associated with the feedback
     */
    private java.util.List<String> tags;

    /**
     * Metadata for the feedback
     */
    private java.util.Map<String, Object> metadata;

    /**
     * Whether the feedback has a response
     */
    private Boolean hasResponse;

    /**
     * Whether the feedback is flagged
     */
    private Boolean isFlagged;

    /**
     * Report count
     */
    private Integer reportCount;

    /**
     * Feedback request ID
     */
    private UUID feedbackRequestId;

    /**
     * When the feedback was published
     */
    private LocalDateTime publishedAt;

    /**
     * When the feedback was archived
     */
    private LocalDateTime archivedAt;

    /**
     * The feedback this response belongs to
     */
    private FeedbackModel feedback;

    /**
     * Content of the response
     */
    private String response;

    /**
     * Whether the response is public
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Update the response content
     */
    public void updateResponse(String newResponse) {
        this.response = newResponse;
    }

    /**
     * Make the response public
     */
    public void makePublic() {
        this.isPublic = true;
    }

    /**
     * Make the response private
     */
    public void makePrivate() {
        this.isPublic = false;
    }

    /**
     * Check if response is visible to author
     */
    public boolean isVisibleToAuthor() {
        return !Boolean.FALSE.equals(this.isPublic);
    }
}
