package com.tellinbox.tellinbox_api.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for Category Score in Feedback.
 * Used to submit scores for specific feedback categories.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoreRequest {

    /**
     * Category UUID
     */
    private UUID categoryId;

    /**
     * Score value (typically 1-5)
     */
    private Integer score;

    /**
     * Optional comment for this specific category
     */
    private String comment;
}
