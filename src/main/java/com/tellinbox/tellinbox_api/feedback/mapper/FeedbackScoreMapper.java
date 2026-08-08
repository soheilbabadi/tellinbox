package com.tellinbox.tellinbox_api.feedback.mapper;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackCategoryModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackScoreModel;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between FeedbackScoreModel and DTOs.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
public class FeedbackScoreMapper {

    /**
     * Converts a FeedbackModel and category info to a FeedbackScoreModel entity.
     * 
     * @param feedback the feedback entity
     * @param category the feedback category
     * @param score the score value
     * @return the created FeedbackScoreModel entity
     */
    public FeedbackScoreModel toEntity(FeedbackModel feedback, FeedbackCategoryModel category, Integer score) {
        return FeedbackScoreModel.builder()
                .feedback(feedback)
                .category(category)
                .receiverId(feedback.getReceiver().getId())
                .score(score)
                .weight(1.0)
                .build();
    }

    /**
     * Converts a FeedbackScoreModel entity to a DTO.
     * For now, returns the entity itself as there's no dedicated Score DTO.
     * Can be extended when a ScoreDto is created.
     * 
     * @param score the score entity
     * @return the score entity (or DTO when created), or null if score is null
     */
    public FeedbackScoreModel toDto(FeedbackScoreModel score) {
        // Currently returning entity as-is since there's no dedicated Score DTO
        // This can be updated when a FeedbackScoreDto is created
        return score;
    }
}
