package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Feedback creation/update requests.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreateRequest {

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
    private String authorIp;
    private String authorUserAgent;
}
