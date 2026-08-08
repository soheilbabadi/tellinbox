package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import lombok.*;

import java.util.UUID;

/**
 * Data Transfer Object for Feedback creation/update requests.
 * This is a general-purpose request DTO that can be used for both create and update operations.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    private UUID receiverId;
    private UUID authorId;
    private Boolean isAnonymous;
    private String title;
    private String content;
    private FeedbackStatus status;
    private FeedbackVisibility visibility;
    private FeedbackPurpose purpose;
    private String relationshipType;
    private Double overallRating;
    private UUID feedbackRequestId;
}
