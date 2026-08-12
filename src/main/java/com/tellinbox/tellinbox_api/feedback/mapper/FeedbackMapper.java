package com.tellinbox.tellinbox_api.feedback.mapper;

import com.tellinbox.tellinbox_api.feedback.dto.FeedbackRequest;
import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.user.enums.RelationshipType;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between FeedbackModel and Feedback DTOs.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
public class FeedbackMapper {

    /**
     * Converts a FeedbackRequest DTO to a FeedbackModel entity.
     * 
     * @param request the feedback request DTO
     * @param author the author user model
     * @param receiver the receiver user model
     * @return the created FeedbackModel entity
     */
    public FeedbackModel toEntity(FeedbackRequest request, UserModel author, UserModel receiver) {
        boolean isAnonymous = Boolean.TRUE.equals(request.getIsAnonymous());
        
        return FeedbackModel.builder()
                .receiver(receiver)
                .author(isAnonymous ? null : author)
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
    }

    /**
     * Converts a FeedbackModel entity to a FeedbackResponse DTO.
     * 
     * @param feedback the feedback entity
     * @return the feedback response DTO, or null if feedback is null
     */
    public FeedbackResponse toDto(FeedbackModel feedback) {
        if (feedback == null) {
            return null;
        }

        RelationshipType relationshipType = feedback.getRelationshipType();
        String relationshipTypePersianName = relationshipType != null ? relationshipType.getPersianName() : null;

        return FeedbackResponse.builder()
                .id(feedback.getId())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .createdBy(feedback.getCreatedBy())
                .updatedBy(feedback.getUpdatedBy())
                .deletedAt(feedback.getDeletedAt())
                .isDeleted(feedback.getIsDeleted())
                .receiverId(feedback.getReceiver() != null ? feedback.getReceiver().getId() : null)
                .receiverName(feedback.getReceiver() != null ? feedback.getReceiver().getDisplayName() : null)
                .authorId(feedback.getAuthor() != null ? feedback.getAuthor().getId() : null)
                .authorName(feedback.getAuthorDisplayName())
                .isAnonymous(feedback.getIsAnonymous())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .status(feedback.getStatus() != null ? feedback.getStatus().name() : null)
                .visibility(feedback.getVisibility() != null ? feedback.getVisibility().name() : null)
                .purpose(feedback.getPurpose() != null ? feedback.getPurpose().name() : null)
                .relationshipType(relationshipType != null ? relationshipType.name() : null)
                .relationshipTypePersianName(relationshipTypePersianName)
                .overallRating(feedback.getOverallRating())
                .isRead(feedback.getIsRead())
                .readAt(feedback.getReadAt())
                .submittedAt(feedback.getCreatedAt())
                .hasResponse(feedback.getHasResponse())
                .isFlagged(feedback.getIsFlagged())
                .reportCount(feedback.getReportCount())
                .feedbackRequestId(feedback.getFeedbackRequestId())
                .publishedAt(feedback.getPublishedAt())
                .archivedAt(feedback.getArchivedAt())
                .response(feedback.getResponse() != null ? feedback.getResponse().getResponse() : null)
                .isPublic(feedback.getResponse() != null ? feedback.getResponse().getIsPublic() : null)
                .build();
    }
}
