package com.tellinbox.tellinbox_api.feedback.model;

import com.tellinbox.tellinbox_api.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a category for feedback scoring.
 * Categories define what aspects of a person are being evaluated.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedback_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_feedback_categories_title", columnNames = "title")
    },
    indexes = {
        @Index(name = "idx_feedback_categories_sort_order", columnList = "sort_order"),
        @Index(name = "idx_feedback_categories_is_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class FeedbackCategoryModel extends BaseEntity {

    /**
     * Category title in Persian
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * Category title in English
     */
    @Column(name = "title_en", length = 100)
    private String titleEn;

    /**
     * Description of the category
     */
    @Column(name = "description", length=4000)
    private String description;

    /**
     * Icon name (FontAwesome or custom icon)
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * Color code for the category
     */
    @Column(name = "color", length = 20)
    private String color;

    /**
     * Sort order for display
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Whether this category is active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this category is a default category
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Minimum score for this category (usually 1)
     */
    @Column(name = "min_score", nullable = false)
    @Builder.Default
    private Integer minScore = 1;

    /**
     * Maximum score for this category (usually 5)
     */
    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Integer maxScore = 5;

    /**
     * Help text for users
     */
    @Column(name = "help_text", length=4000)
    private String helpText;

    /**
     * One-to-Many relationship with FeedbackScore
     */
    @OneToMany(
        mappedBy = "category",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeedbackScoreModel> scores = new ArrayList<>();

    /**
     * Get display title based on language
     */
    public String getDisplayTitle(String language) {
        if ("en".equals(language) && this.titleEn != null) {
            return this.titleEn;
        }
        return this.title;
    }

    /**
     * Get emoji representation or icon
     */
    public String getIconDisplay() {
        if (this.icon != null && this.icon.startsWith("emoji:")) {
            return this.icon.substring(6);
        }
        return "📊";
    }
}