package com.tellinbox.tellinbox_api.feedback.model;

import com.tellinbox.tellinbox_api.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
@EqualsAndHashCode(callSuper = true)
public class FeedbackScoreModel extends BaseEntity {

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