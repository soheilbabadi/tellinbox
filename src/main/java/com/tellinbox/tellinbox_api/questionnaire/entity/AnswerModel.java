package com.tellinbox.tellinbox_api.questionnaire.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing an answer to a question in a questionnaire response.
 */
@Entity
@Table(
    name = "question_answers",
    indexes = {
        @Index(name = "idx_question_answers_response_id", columnList = "response_id"),
        @Index(name = "idx_question_answers_question_id", columnList = "question_id"),
        @Index(name = "idx_question_answers_option_id", columnList = "option_id")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"response", "question", "selectedOption"})
public class AnswerModel {

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
     * The response this answer belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answers_response"))
    private QuestionnaireResponseModel response;

    /**
     * The question this answer is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answers_question"))
    private QuestionModel question;

    /**
     * The selected option (for multiple choice questions)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", foreignKey = @ForeignKey(name = "fk_answers_option"))
    private QuestionOptionModel selectedOption;

    /**
     * Text answer (for short/long answer questions)
     */
    @Column(name = "text_answer", length = 4000)
    private String textAnswer;

    /**
     * Numeric answer (for number questions)
     */
    @Column(name = "number_answer")
    private Double numberAnswer;

    /**
     * Boolean answer (for yes/no, true/false questions)
     */
    @Column(name = "boolean_answer")
    private Boolean booleanAnswer;

    /**
     * Date answer (for date questions)
     */
    @Column(name = "date_answer")
    private LocalDateTime dateAnswer;

    /**
     * Score value for this answer (for quizzes)
     */
    @Column(name = "score_value")
    private Double scoreValue;

    /**
     * Whether this answer is correct (for quizzes)
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;

    /**
     * Soft delete the answer
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted answer
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if answer is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
