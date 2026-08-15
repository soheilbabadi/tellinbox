package com.tellinbox.tellinbox_api.questionnaire.entity;

import com.tellinbox.tellinbox_api.question.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity class representing a Question in a questionnaire.
 */
@Entity
@Table(
    name = "questions",
    indexes = {
        @Index(name = "idx_questions_questionnaire_id", columnList = "questionnaire_id"),
        @Index(name = "idx_questions_order", columnList = "\"order\""),
        @Index(name = "idx_questions_is_required", columnList = "is_required"),
        @Index(name = "idx_questions_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"questionnaire", "options", "answers"})
public class QuestionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    protected Boolean isDeleted = false;

    /**
     * The questionnaire this question belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questions_questionnaire"))
    private QuestionnaireModel questionnaire;

    /**
     * Order of the question in the questionnaire
     */
    @Column(name = "\"order\"", nullable = false)
    private Integer order;

    /**
     * Question text
     */
    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    /**
     * Question description/help text
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Type of the question (MULTIPLE_CHOICE, TRUE_FALSE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private QuestionType type;

    /**
     * Whether this question is required
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /**
     * Placeholder text for input fields
     */
    @Column(name = "placeholder", length = 200)
    private String placeholder;

    /**
     * Minimum value for number/date questions
     */
    @Column(name = "min_value")
    private Double minValue;

    /**
     * Maximum value for number/date questions
     */
    @Column(name = "max_value")
    private Double maxValue;

    /**
     * Regex pattern for validation (short/long answer)
     */
    @Column(name = "validation_pattern", length = 200)
    private String validationPattern;

    /**
     * Custom error message for validation failures
     */
    @Column(name = "validation_error_message", length = 500)
    private String validationErrorMessage;

    /**
     * Default value for the question
     */
    @Column(name = "default_value", length = 500)
    private String defaultValue;

    // ==================== Relationships ====================

    /**
     * Options for multiple choice questions
     */
    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("order ASC")
    @Builder.Default
    private List<QuestionOptionModel> options = new ArrayList<>();

    /**
     * Answers to this question
     */
    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<AnswerModel> answers = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Add an option to this question
     */
    public void addOption(QuestionOptionModel option) {
        option.setQuestion(this);
        this.options.add(option);
    }

    /**
     * Remove an option from this question
     */
    public void removeOption(QuestionOptionModel option) {
        this.options.remove(option);
        option.setQuestion(null);
    }

    /**
     * Soft delete the question
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted question
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if question is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
