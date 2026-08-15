package com.tellinbox.tellinbox_api.questionnaire.dto;

import com.tellinbox.tellinbox_api.question.enums.QuestionType;
import com.tellinbox.tellinbox_api.questionnaire.enums.QuestionnaireStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a questionnaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireCreateRequest {

    /**
     * Title of the questionnaire
     */
    @NonNull
    private String title;

    /**
     * Description of the questionnaire
     */
    private String description;

    /**
     * Welcome message shown before starting
     */
    private String welcomeMessage;

    /**
     * Thank you message shown after completion
     */
    private String thankYouMessage;

    /**
     * Whether to allow anonymous responses
     */
    @Builder.Default
    private Boolean allowAnonymous = false;

    /**
     * Whether to show results to respondents
     */
    @Builder.Default
    private Boolean showResults = false;

    /**
     * Maximum number of responses (null = unlimited)
     */
    private Integer maxResponses;

    /**
     * Start date for accepting responses
     */
    private LocalDateTime startDate;

    /**
     * End date for accepting responses
     */
    private LocalDateTime endDate;

    /**
     * Questions in the questionnaire
     */
    @Builder.Default
    private List<QuestionCreateRequest> questions = List.of();

    /**
     * Request DTO for creating a question
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionCreateRequest {

        /**
         * Question text
         */
        @NonNull
        private String text;

        /**
         * Question description/help text
         */
        private String description;

        /**
         * Type of the question
         */
        @NonNull
        private QuestionType type;

        /**
         * Whether this question is required
         */
        @Builder.Default
        private Boolean isRequired = true;

        /**
         * Placeholder text for input fields
         */
        private String placeholder;

        /**
         * Minimum value for number/date questions
         */
        private Double minValue;

        /**
         * Maximum value for number/date questions
         */
        private Double maxValue;

        /**
         * Regex pattern for validation
         */
        private String validationPattern;

        /**
         * Custom error message for validation failures
         */
        private String validationErrorMessage;

        /**
         * Default value for the question
         */
        private String defaultValue;

        /**
         * Options for multiple choice questions
         */
        @Builder.Default
        private List<OptionCreateRequest> options = List.of();

        /**
         * Request DTO for creating an option
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OptionCreateRequest {

            /**
             * Option text
             */
            @NonNull
            private String text;

            /**
             * Order of the option
             */
            private Integer order;

            /**
             * Whether this is the correct answer (for quizzes)
             */
            @Builder.Default
            private Boolean isCorrect = false;

            /**
             * Score value for this option
             */
            private Double scoreValue;

            /**
             * Explanation for this option
             */
            private String explanation;
        }
    }
}
