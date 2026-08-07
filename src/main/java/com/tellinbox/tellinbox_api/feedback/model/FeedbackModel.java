package com.tellinbox.tellinbox_api.feedback.model;

import com.tellinbox.tellinbox_api.feedback.dto.FeedbackResponse;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackStatus;
import com.tellinbox.tellinbox_api.feedback.enums.FeedbackVisibility;
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
 * Entity class representing a feedback in the Tellinbox system.
 * Each feedback is created by an author (could be anonymous) for a receiver.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_feedbacks_request_id", 
            columnNames = "feedback_request_id"
        )
    },
    indexes = {
        @Index(name = "idx_feedbacks_receiver_id", columnList = "receiver_id"),
        @Index(name = "idx_feedbacks_author_id", columnList = "author_id"),
        @Index(name = "idx_feedbacks_status", columnList = "status"),
        @Index(name = "idx_feedbacks_visibility", columnList = "visibility"),
        @Index(name = "idx_feedbacks_purpose", columnList = "purpose"),
        @Index(name = "idx_feedbacks_created_at", columnList = "created_at"),
        @Index(name = "idx_feedbacks_receiver_status", columnList = "receiver_id, status"),
        @Index(name = "idx_feedbacks_author_anonymous", columnList = "author_id, is_anonymous"),
        @Index(name = "idx_feedbacks_updated_at", columnList = "updated_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"receiver", "author", "scores", "response", "answers"})
@EqualsAndHashCode(callSuper = false)
public class FeedbackModel {

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
     * The user who receives this feedback
     * This is the owner of the feedback
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "receiver_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedbacks_receiver")
    )
    private UserModel receiver;

    /**
     * The user who writes this feedback (if not anonymous)
     * Can be null for anonymous feedbacks
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "author_id",
        foreignKey = @ForeignKey(name = "fk_feedbacks_author")
    )
    private UserModel author;

    /**
     * Indicates if this feedback is anonymous
     * If true, author_id should be null
     */
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = true;

    /**
     * Title/summary of the feedback
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Main content of the feedback
     */
    @Column(name = "content", nullable = false, length=4000)
    private String content;

    /**
     * Status of the feedback (PENDING, PUBLISHED, ARCHIVED, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.PENDING;

    /**
     * Visibility level of the feedback
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private FeedbackVisibility visibility = FeedbackVisibility.PRIVATE;

    /**
     * Purpose/context of this feedback
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 30)
    private FeedbackPurpose purpose;

    /**
     * Relationship type between author and receiver
     * (FRIEND, COLLEAGUE, MANAGER, etc.)
     */
    @Column(name = "relationship_type", length = 30)
    private String relationshipType;

    /**
     * Overall rating (1-5 stars)
     */
    @Column(name = "overall_rating")
    @Builder.Default
    private Double overallRating = 0.0;

    /**
     * Whether the feedback has been read by the receiver
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Timestamp when the feedback was read
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Whether the feedback has been responded to
     */
    @Column(name = "has_response", nullable = false)
    @Builder.Default
    private Boolean hasResponse = false;

    /**
     * Whether the feedback has been flagged/reported
     */
    @Column(name = "is_flagged", nullable = false)
    @Builder.Default
    private Boolean isFlagged = false;

    /**
     * Number of times this feedback has been reported
     */
    @Column(name = "report_count")
    @Builder.Default
    private Integer reportCount = 0;

    /**
     * Feedback request token (if feedback was submitted via a request link)
     */
    @Column(name = "feedback_request_id")
    private UUID feedbackRequestId;

    /**
     * IP address of the author (for security/audit)
     */
    @Column(name = "author_ip", length = 45)
    private String authorIp;

    /**
     * User agent of the author (for analytics)
     */
    @Column(name = "author_user_agent", length = 500)
    private String authorUserAgent;

    /**
     * Timestamp when feedback was archived
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    /**
     * Who archived this feedback
     */
    @Column(name = "archived_by")
    private UUID archivedBy;

    /**
     * Timestamp when feedback was published
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // ==================== Relationships ====================

    /**
     * Scores for different categories
     */
    @OneToMany(
        mappedBy = "feedback",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeedbackScoreModel> scores = new ArrayList<>();

    /**
     * Response to this feedback (if any)
     */
    @OneToOne(
        mappedBy = "feedback",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private FeedbackResponse response;

    /**
     * Reports/flags for this feedback
     */
    @OneToMany(
        mappedBy = "feedback",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FeedbackReportModel> reports = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Mark feedback as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark feedback as flagged
     */
    public void flag() {
        this.isFlagged = true;
        this.reportCount = (this.reportCount == null ? 0 : this.reportCount) + 1;
    }

    /**
     * Unflag feedback
     */
    public void unflag() {
        this.isFlagged = false;
        this.reportCount = 0;
    }

    /**
     * Increment report count
     */
    public void incrementReportCount() {
        this.reportCount = (this.reportCount == null ? 0 : this.reportCount) + 1;
        if (this.reportCount >= 5) {
            this.isFlagged = true;
        }
    }

    /**
     * Publish the feedback
     */
    public void publish() {
        this.status = FeedbackStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.visibility = FeedbackVisibility.PRIVATE;
    }

    /**
     * Archive the feedback
     */
    public void archive(UUID archivedBy) {
        this.status = FeedbackStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
        this.archivedBy = archivedBy;
    }

    /**
     * Soft delete the feedback
     */
    public void softDelete() {
        this.status = FeedbackStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Check if feedback is active
     */
    public boolean isActive() {
        return this.status == FeedbackStatus.PUBLISHED && !Boolean.TRUE.equals(this.isDeleted);
    }

    /**
     * Check if author is anonymous
     */
    public boolean isAnonymous() {
        return Boolean.TRUE.equals(this.isAnonymous) || this.author == null;
    }

    /**
     * Get author display name
     */
    public String getAuthorDisplayName() {
        if (isAnonymous()) {
            return "ناشناس";
        }
        return this.author != null ? this.author.getDisplayName() : "ناشناس";
    }

    /**
     * Check if feedback can be responded to
     */
    public boolean canRespond() {
        return isActive() && !Boolean.TRUE.equals(this.hasResponse);
    }

    /**
     * Add a score to this feedback
     */
    public void addScore(FeedbackCategoryModel category, Integer score) {
        FeedbackScoreModel feedbackScoreModel = FeedbackScoreModel.builder()
            .feedback(this)
            .category(category)
            .score(score)
            .build();
        this.scores.add(feedbackScoreModel);
        // Recalculate overall rating
        this.calculateOverallRating();
    }

    /**
     * Calculate overall rating from scores
     */
    private void calculateOverallRating() {
        if (scores.isEmpty()) {
            this.overallRating = 0.0;
            return;
        }
        double sum = scores.stream()
            .mapToInt(FeedbackScoreModel::getScore)
            .sum();
        this.overallRating = Math.round((sum / scores.size()) * 10.0) / 10.0;
    }

    /**
     * Add a report to this feedback
     */
    public void addReport(FeedbackReportModel report) {
        this.reports.add(report);
        this.incrementReportCount();
    }

    /**
     * Add a response to this feedback
     */
    public void addResponse(FeedbackResponse response) {
        this.response = response;
        this.hasResponse = true;
    }

    /**
     * Get average score for a specific category
     */
    public Double getAverageScoreForCategory(UUID categoryId) {
        return scores.stream()
            .filter(score -> score.getCategory().getId().equals(categoryId))
            .mapToInt(FeedbackScoreModel::getScore)
            .average()
            .orElse(0.0);
    }

    /**
     * Builder with default values
     */
    public static class FeedbackBuilder {
        private FeedbackStatus status = FeedbackStatus.PENDING;
        private FeedbackVisibility visibility = FeedbackVisibility.PRIVATE;
        private Boolean isAnonymous = true;
        private Boolean isRead = false;
        private Boolean hasResponse = false;
        private Boolean isFlagged = false;
        private Integer reportCount = 0;
        private Double overallRating = 0.0;
        private List<FeedbackScoreModel> scores = new ArrayList<>();
        private List<FeedbackReportModel> reports = new ArrayList<>();
    }
}