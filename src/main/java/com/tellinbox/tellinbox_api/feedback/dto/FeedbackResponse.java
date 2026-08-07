package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.base.BaseEntity;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import jakarta.persistence.*;
import lombok.*;

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
@EqualsAndHashCode(callSuper = true)
public class FeedbackResponse extends BaseEntity {

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