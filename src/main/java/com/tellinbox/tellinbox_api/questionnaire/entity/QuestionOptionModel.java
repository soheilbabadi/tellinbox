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
 * Entity class representing an option for a multiple choice question.
 */
@Entity
@Table(
    name = "question_options",
    indexes = {
        @Index(name = "idx_question_options_question_id", columnList = "question_id"),
        @Index(name = "idx_question_options_order", columnList = "\"order\""),
        @Index(name = "idx_question_options_is_correct", columnList = "is_correct")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"question"})
public class QuestionOptionModel {

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
     * The question this option belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_options_question"))
    private QuestionModel question;

    /**
     * Order of the option
     */
    @Column(name = "\"order\"", nullable = false)
    private Integer order;

    /**
     * Option text
     */
    @Column(name = "text", nullable = false, length = 500)
    private String text;

    /**
     * Whether this is the correct answer (for quizzes)
     */
    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    /**
     * Score value for this option (for quizzes)
     */
    @Column(name = "score_value")
    private Double scoreValue;

    /**
     * Additional explanation for this option
     */
    @Column(name = "explanation", length = 500)
    private String explanation;

    /**
     * Soft delete the option
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted option
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if option is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
