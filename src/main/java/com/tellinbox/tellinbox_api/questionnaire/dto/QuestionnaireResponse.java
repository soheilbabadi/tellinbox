package com.tellinbox.tellinbox_api.questionnaire.dto;

import com.tellinbox.tellinbox_api.question.enums.QuestionType;
import com.tellinbox.tellinbox_api.questionnaire.enums.QuestionnaireStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for questionnaire response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireResponse {

    /**
     * Questionnaire ID
     */
    private UUID id;

    /**
     * Title of the questionnaire
     */
    private String title;

    /**
     * Description of the questionnaire
     */
    private String description;

    /**
     * Welcome message
     */
    private String welcomeMessage;

    /**
     * Thank you message
     */
    private String thankYouMessage;

    /**
     * Organization ID
     */
    private UUID organizationId;

    /**
     * Organization name
     */
    private String organizationName;

    /**
     * Owner ID
     */
    private UUID ownerId;

    /**
     * Owner display name
     */
    private String ownerDisplayName;

    /**
     * Status of the questionnaire
     */
    private QuestionnaireStatus status;

    /**
     * Whether the questionnaire is active
     */
    private Boolean isActive;

    /**
     * Start date for accepting responses
     */
    private LocalDateTime startDate;

    /**
     * End date for accepting responses
     */
    private LocalDateTime endDate;

    /**
     * Whether anonymous responses are allowed
     */
    private Boolean allowAnonymous;

    /**
     * Maximum number of responses
     */
    private Integer maxResponses;

    /**
     * Current number of responses
     */
    private Integer responseCount;

    /**
     * Whether to show results to respondents
     */
    private Boolean showResults;

    /**
     * Creation date
     */
    private LocalDateTime createdAt;

    /**
     * Last update date
     */
    private LocalDateTime updatedAt;

    /**
     * Questions in the questionnaire
     */
    private List<QuestionResponse> questions;

    /**
     * DTO for question response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponse {

        /**
         * Question ID
         */
        private UUID id;

        /**
         * Question text
         */
        private String text;

        /**
         * Question description
         */
        private String description;

        /**
         * Question type
         */
        private QuestionType type;

        /**
         * Whether the question is required
         */
        private Boolean isRequired;

        /**
         * Placeholder text
         */
        private String placeholder;

        /**
         * Minimum value
         */
        private Double minValue;

        /**
         * Maximum value
         */
        private Double maxValue;

        /**
         * Validation pattern
         */
        private String validationPattern;

        /**
         * Validation error message
         */
        private String validationErrorMessage;

        /**
         * Default value
         */
        private String defaultValue;

        /**
         * Order of the question
         */
        private Integer order;

        /**
         * Options for multiple choice questions
         */
        private List<OptionResponse> options;

        /**
         * DTO for option response
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OptionResponse {

            /**
             * Option ID
             */
            private UUID id;

            /**
             * Option text
             */
            private String text;

            /**
             * Order of the option
             */
            private Integer order;

            /**
             * Whether this is the correct answer
             */
            private Boolean isCorrect;

            /**
             * Score value
             */
            private Double scoreValue;

            /**
             * Explanation
             */
            private String explanation;
        }
    }
}
