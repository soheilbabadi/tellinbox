package com.tellinbox.tellinbox_api.feedback.model;

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
 * Entity representing a score for a specific category in a feedback.
 * Each feedback can have multiple scores for different categories.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedback_scores",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_feedback_scores_feedback_category",
            columnNames = {"feedback_id", "category_id"}
        )
    },
    indexes = {
        @Index(name = "idx_feedback_scores_feedback_id", columnList = "feedback_id"),
        @Index(name = "idx_feedback_scores_category_id", columnList = "category_id"),
        @Index(name = "idx_feedback_scores_score", columnList = "score")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"feedback", "category"})
@EqualsAndHashCode(callSuper = false)
public class FeedbackScoreModel {

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
    protected Boolean isDeleted = false;

    /**
     * The feedback this score belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "feedback_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedback_scores_feedback")
    )
    private FeedbackModel feedback;

    /**
     * The category being scored
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedback_scores_category")
    )
    private FeedbackCategoryModel category;

    /**
     * Score value (typically 1-5)
     */
    @Column(name = "score", nullable = false)
    private Integer score;

    /**
     * Optional comment for this specific score
     */
    @Column(name = "comment", length = 4000)
    private String comment;

    /**
     * Weight of this score in overall rating
     */
    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Double weight = 1.0;

    /**
     * Validate score is within range
     */
    public boolean isValidScore() {
        Integer min = this.category != null ? this.category.getMinScore() : 1;
        Integer max = this.category != null ? this.category.getMaxScore() : 5;
        return this.score != null && this.score >= min && this.score <= max;
    }

    /**
     * Get normalized score (0-1)
     */
    public Double getNormalizedScore() {
        if (this.score == null) {
            return 0.0;
        }
        Integer min = this.category != null ? this.category.getMinScore() : 1;
        Integer max = this.category != null ? this.category.getMaxScore() : 5;
        return (double) (this.score - min) / (max - min);
    }

    /**
     * Get weighted score
     */
    public Double getWeightedScore() {
        return this.getNormalizedScore() * this.weight;
    }
}