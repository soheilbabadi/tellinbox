package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
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
 * Entity representing a response to a feedback.
 * The receiver can respond to feedbacks they've received.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedback_responses",
    indexes = {
        @Index(name = "idx_feedback_responses_feedback_id", columnList = "feedback_id"),
        @Index(name = "idx_feedback_responses_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"feedback"})
@EqualsAndHashCode(callSuper = false)
public class FeedbackResponse {

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
     * The feedback this response belongs to
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "feedback_id", 
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_feedback_responses_feedback")
    )
    private FeedbackModel feedback;

    /**
     * Content of the response
     */
    @Column(name = "response", nullable = false, length=4000)
    private String response;

    /**
     * Whether the response is public
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Update the response content
     */
    public void updateResponse(String newResponse) {
        this.response = newResponse;
    }

    /**
     * Make the response public
     */
    public void makePublic() {
        this.isPublic = true;
    }

    /**
     * Make the response private
     */
    public void makePrivate() {
        this.isPublic = false;
    }

    /**
     * Check if response is visible to author
     */
    public boolean isVisibleToAuthor() {
        return !Boolean.FALSE.equals(this.isPublic);
    }
}
