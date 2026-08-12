package com.tellinbox.tellinbox_api.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object representing a feedback category with its score.
 * Used in responses to show category-wise scores.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoreDto {

    /**
     * Category UUID
     */
    private UUID categoryId;

    /**
     * Category title (in Persian)
     */
    private String categoryTitle;

    /**
     * Category title in English
     */
    private String categoryTitleEn;

    /**
     * Score value (typically 1-5)
     */
    private Integer score;

    /**
     * Optional comment for this specific category
     */
    private String comment;

    /**
     * Minimum possible score for this category
     */
    private Integer minScore;

    /**
     * Maximum possible score for this category
     */
    private Integer maxScore;

    /**
     * Icon representation for the category
     */
    private String icon;

    /**
     * Color code for the category
     */
    private String color;
}
