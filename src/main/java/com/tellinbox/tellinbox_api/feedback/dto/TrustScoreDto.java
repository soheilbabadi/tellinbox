package com.tellinbox.tellinbox_api.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO for Trust Score response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Calculated trust score (0-100)
     */
    private Double score;

    /**
     * Score level (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String level;

    /**
     * Number of feedbacks used in calculation
     */
    private Long feedbackCount;

    /**
     * Number of unique authors
     */
    private Long uniqueAuthorsCount;

    /**
     * Average author credibility score
     */
    private Double averageAuthorCredibility;

    /**
     * Recency factor (0-1)
     */
    private Double recencyFactor;

    /**
     * Base rating average
     */
    private Double averageRating;

    /**
     * Breakdown of score components
     */
    private TrustScoreComponents components;

    /**
     * Inner class for score components breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrustScoreComponents implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Weight contribution from feedback count
         */
        private Double countComponent;

        /**
         * Weight contribution from author diversity
         */
        private Double diversityComponent;

        /**
         * Weight contribution from author credibility
         */
        private Double credibilityComponent;

        /**
         * Weight contribution from recency
         */
        private Double recencyComponent;

        /**
         * Weight contribution from average rating
         */
        private Double ratingComponent;
    }
}
