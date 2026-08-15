package com.tellinbox.tellinbox_api.questionnaire.entity;

import com.tellinbox.tellinbox_api.user.model.UserModel;
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
 * Entity class representing a response to a questionnaire.
 */
@Entity
@Table(
    name = "questionnaire_responses",
    indexes = {
        @Index(name = "idx_questionnaire_responses_questionnaire_id", columnList = "questionnaire_id"),
        @Index(name = "idx_questionnaire_responses_respondent_id", columnList = "respondent_id"),
        @Index(name = "idx_questionnaire_responses_submitted_at", columnList = "submitted_at"),
        @Index(name = "idx_questionnaire_responses_is_complete", columnList = "is_complete")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"questionnaire", "respondent", "answers"})
public class QuestionnaireResponseModel {

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
     * The questionnaire this response is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_responses_questionnaire"))
    private QuestionnaireModel questionnaire;

    /**
     * The user who submitted this response (null if anonymous)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", foreignKey = @ForeignKey(name = "fk_responses_respondent"))
    private UserModel respondent;

    /**
     * Whether this response was submitted anonymously
     */
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;

    /**
     * IP address of the respondent
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of the respondent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Time taken to complete the questionnaire (in seconds)
     */
    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    /**
     * When the response was started
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * When the response was submitted
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Whether the response is complete
     */
    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private Boolean isComplete = false;

    /**
     * Total score for the response (for quizzes)
     */
    @Column(name = "total_score")
    private Double totalScore;

    /**
     * Comments or notes about this response
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    // ==================== Relationships ====================

    /**
     * Answers to individual questions
     */
    @OneToMany(
        mappedBy = "response",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<AnswerModel> answers = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Add an answer to this response
     */
    public void addAnswer(AnswerModel answer) {
        answer.setResponse(this);
        this.answers.add(answer);
    }

    /**
     * Remove an answer from this response
     */
    public void removeAnswer(AnswerModel answer) {
        this.answers.remove(answer);
        answer.setResponse(null);
    }

    /**
     * Mark response as complete
     */
    public void complete() {
        this.isComplete = true;
        this.submittedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.timeTakenSeconds = (int) (this.submittedAt.toEpochSecond(java.time.ZoneOffset.UTC) - 
                                           this.startedAt.toEpochSecond(java.time.ZoneOffset.UTC));
        }
    }

    /**
     * Start the response
     */
    public void start() {
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Calculate total score from answers
     */
    public void calculateTotalScore() {
        if (this.answers.isEmpty()) {
            this.totalScore = 0.0;
            return;
        }
        double sum = this.answers.stream()
            .filter(answer -> answer.getScoreValue() != null)
            .mapToDouble(AnswerModel::getScoreValue)
            .sum();
        this.totalScore = Math.round(sum * 10.0) / 10.0;
    }

    /**
     * Soft delete the response
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted response
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if response is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
