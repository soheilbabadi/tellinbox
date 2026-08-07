package com.tellinbox.tellinbox_api.feedback.model;

import com.tellinbox.tellinbox_api.base.BaseEntity;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a report for inappropriate feedback.
 * Users can report feedbacks that violate guidelines.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedback_reports",
    indexes = {
        @Index(name = "idx_feedback_reports_feedback_id", columnList = "feedback_id"),
        @Index(name = "idx_feedback_reports_reporter_id", columnList = "reporter_id"),
        @Index(name = "idx_feedback_reports_status", columnList = "status"),
        @Index(name = "idx_feedback_reports_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"feedback", "reporter"})
@EqualsAndHashCode(callSuper = true)
public class FeedbackReportModel extends BaseEntity {

    /**
     * The feedback being reported
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "feedback_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedback_reports_feedback")
    )
    private FeedbackModel feedback;

    /**
     * The user reporting the feedback
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reporter_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedback_reports_reporter")
    )
    private UserModel reporter;

    /**
     * Reason for reporting
     */
    @Column(name = "reason", nullable = false, length = 50)
    private String reason;

    /**
     * Detailed description of the report
     */
    @Column(name = "description", length=4000)
    private String description;

    /**
     * Status of the report (PENDING, REVIEWED, RESOLVED, REJECTED)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * Admin notes
     */
    @Column(name = "admin_notes", length=4000)
    private String adminNotes;

    /**
     * Who resolved this report (admin user ID)
     */
    @Column(name = "resolved_by")
    private UUID resolvedBy;

    /**
     * Timestamp when resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Resolve the report
     */
    public void resolve(UUID adminId, String notes) {
        this.status = "RESOLVED";
        this.resolvedBy = adminId;
        this.resolvedAt = LocalDateTime.now();
        this.adminNotes = notes;
    }

    /**
     * Reject the report
     */
    public void reject(UUID adminId, String reason) {
        this.status = "REJECTED";
        this.resolvedBy = adminId;
        this.resolvedAt = LocalDateTime.now();
        this.adminNotes = reason;
    }

    /**
     * Check if report is pending
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}