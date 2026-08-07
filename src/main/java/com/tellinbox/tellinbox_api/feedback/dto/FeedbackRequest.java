package com.tellinbox.tellinbox_api.feedback.dto;

import com.tellinbox.tellinbox_api.feedback.enums.FeedbackPurpose;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a feedback request.
 * Users can create requests to collect feedback from specific people.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Entity
@Table(
    name = "feedback_requests",
    indexes = {
        @Index(name = "idx_feedback_requests_user_id", columnList = "user_id"),
        @Index(name = "idx_feedback_requests_token", columnList = "token"),
        @Index(name = "idx_feedback_requests_purpose", columnList = "purpose"),
        @Index(name = "idx_feedback_requests_is_active", columnList = "is_active"),
        @Index(name = "idx_feedback_requests_expire_date", columnList = "expire_date"),
        @Index(name = "idx_feedback_requests_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(of = "id")
public class FeedbackRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * The user who created this request (the receiver of feedbacks)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id", 
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedback_requests_user")
    )
    private UserModel user;

    /**
     * Title of the feedback request
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Description of the feedback request
     */
    @Column(name = "description",length = 4000)
    private String description;

    /**
     * Purpose of this feedback request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private FeedbackPurpose purpose;

    /**
     * Unique token for the request link
     * Example: feedback.com/r/abc123xyz
     */
    @Column(name = "token", nullable = false, unique = true, length = 50)
    private String token;

    /**
     * URL-friendly slug
     * Example: feedback.com/soheil-ahmadi
     */
    @Column(name = "slug", unique = true, length = 100)
    private String slug;

    /**
     * Whether the request is active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Expiration date of the request (null = never expires)
     */
    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    /**
     * Maximum number of responses allowed
     */
    @Column(name = "max_responses")
    private Integer maxResponses;

    /**
     * Current number of responses received
     */
    @Column(name = "response_count")
    @Builder.Default
    private Integer responseCount = 0;

    /**
     * Whether to allow anonymous responses
     */
    @Column(name = "allow_anonymous", nullable = false)
    @Builder.Default
    private Boolean allowAnonymous = true;

    /**
     * Whether to require authentication
     */
    @Column(name = "require_authentication", nullable = false)
    @Builder.Default
    private Boolean requireAuthentication = false;

    /**
     * Whether to notify user when feedback is received
     */
    @Column(name = "notify_on_feedback", nullable = false)
    @Builder.Default
    private Boolean notifyOnFeedback = true;

    /**
     * Custom thank you message after submission
     */
    @Column(name = "thank_you_message", length=4000)
    private String thankYouMessage;

    /**
     * Creation timestamp
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if the request is still valid
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(this.isActive) && 
               (this.expireDate == null || this.expireDate.isAfter(LocalDateTime.now())) &&
               (this.maxResponses == null || this.responseCount < this.maxResponses);
    }

    /**
     * Check if the request has expired
     */
    public boolean isExpired() {
        return this.expireDate != null && this.expireDate.isBefore(LocalDateTime.now());
    }

    /**
     * Increment response count
     */
    public void incrementResponseCount() {
        this.responseCount = (this.responseCount == null ? 0 : this.responseCount) + 1;
    }

    /**
     * Get remaining responses allowed
     */
    public Integer getRemainingResponses() {
        if (this.maxResponses == null) {
            return null;
        }
        return Math.max(0, this.maxResponses - (this.responseCount == null ? 0 : this.responseCount));
    }

    /**
     * Deactivate the request
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate the request
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Get the full URL for this request
     */
    public String getFullUrl(String baseUrl) {
        return baseUrl + "/r/" + this.token;
    }

    /**
     * Get the slug URL
     */
    public String getSlugUrl(String baseUrl) {
        return baseUrl + "/" + this.slug;
    }
}