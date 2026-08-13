package com.tellinbox.tellinbox_api.feedback.mapper;

import com.tellinbox.tellinbox_api.feedback.dto.CategoryScoreDto;
import com.tellinbox.tellinbox_api.feedback.dto.CategoryScoreRequest;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackCategoryModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackScoreModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between FeedbackScoreModel and Category Score DTOs.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
public class CategoryScoreMapper {

    /**
     * Converts a CategoryScoreRequest DTO to a FeedbackScoreModel entity.
     * 
     * @param request the category score request DTO
     * @param feedback the feedback entity
     * @param category the category entity
     * @return the created FeedbackScoreModel entity
     */
    public FeedbackScoreModel toEntity(CategoryScoreRequest request, FeedbackModel feedback, FeedbackCategoryModel category) {
        return FeedbackScoreModel.builder()
                .feedback(feedback)
                .category(category)
                .receiverId(feedback.getReceiver().getId())
                .score(request.getScore())
                .comment(request.getComment())
                .weight(1.0)
                .build();
    }

    /**
     * Converts a FeedbackScoreModel entity to a CategoryScoreDto.
     * 
     * @param score the score entity
     * @return the category score DTO, or null if score is null
     */
    public CategoryScoreDto toDto(FeedbackScoreModel score) {
        if (score == null) {
            return null;
        }

        FeedbackCategoryModel category = score.getCategory();
        
        return CategoryScoreDto.builder()
                .categoryId(category != null ? category.getId() : null)
                .categoryTitle(category != null ? category.getTitle() : null)
                .categoryTitleEn(category != null ? category.getTitleEn() : null)
                .score(score.getScore())
                .comment(score.getComment())
                .minScore(category != null ? category.getMinScore() : 1)
                .maxScore(category != null ? category.getMaxScore() : 5)
                .icon(category != null ? category.getIcon() : null)
                .color(category != null ? category.getColor() : null)
                .build();
    }

    /**
     * Convert list of scores to DTOs
     */
    public List<CategoryScoreDto> toDtoList(List<FeedbackScoreModel> scores) {
        if (scores == null) {
            return null;
        }
        return scores.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
