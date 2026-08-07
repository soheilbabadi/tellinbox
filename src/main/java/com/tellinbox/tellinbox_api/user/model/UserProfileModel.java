package com.tellinbox.tellinbox_api.user.model;

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
 * User profile entity containing user preferences and settings.
 * This is separated from User to avoid cluttering the core user table.
 */
@Entity
@Table(
    name = "user_profiles",
    indexes = {
        @Index(name = "idx_user_profiles_user_id", columnList = "user_id"),
        @Index(name = "idx_user_profiles_public_link", columnList = "public_link")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
@AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false, updatable = false))
public class UserProfileModel {

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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    /**
     * Public link for receiving feedback
     * Example: tellinbox.com/soheil_ahmadi
     */
    @Column(name = "public_link", unique = true, length = 100)
    private String publicLink;

    /**
     * Whether user accepts anonymous feedback
     */
    @Column(name = "receive_anonymous_feedback", nullable = false)
    @Builder.Default
    private Boolean receiveAnonymousFeedback = true;

    /**
     * Whether user accepts named feedback
     */
    @Column(name = "receive_named_feedback", nullable = false)
    @Builder.Default
    private Boolean receiveNamedFeedback = true;

    /**
     * Whether to show statistics on public profile
     */
    @Column(name = "show_statistics", nullable = false)
    @Builder.Default
    private Boolean showStatistics = true;

    /**
     * Whether to show average score on public profile
     */
    @Column(name = "show_average_score", nullable = false)
    @Builder.Default
    private Boolean showAverageScore = true;

    /**
     * Whether to enable AI analysis
     */
    @Column(name = "enable_ai_analysis", nullable = false)
    @Builder.Default
    private Boolean enableAiAnalysis = true;

    /**
     * Whether to receive email notifications
     */
    @Column(name = "receive_email_notifications", nullable = false)
    @Builder.Default
    private Boolean receiveEmailNotifications = true;

    /**
     * Whether to receive SMS notifications
     */
    @Column(name = "receive_sms_notifications", nullable = false)
    @Builder.Default
    private Boolean receiveSmsNotifications = true;

    /**
     * Whether to receive push notifications
     */
    @Column(name = "receive_push_notifications", nullable = false)
    @Builder.Default
    private Boolean receivePushNotifications = true;

    /**
     * Maximum number of feedbacks to show per page
     */
    @Column(name = "items_per_page")
    @Builder.Default
    private Integer itemsPerPage = 20;

    /**
     * Preferred theme (light/dark)
     */
    @Column(name = "theme", length = 10)
    @Builder.Default
    private String theme = "light";

    /**
     * LinkedIn profile URL
     */
    @Column(name = "linkedin_url", length = 200)
    private String linkedinUrl;

    /**
     * Twitter/X profile URL
     */
    @Column(name = "twitter_url", length = 200)
    private String twitterUrl;

    /**
     * GitHub profile URL
     */
    @Column(name = "github_url", length = 200)
    private String githubUrl;

    /**
     * Personal website URL
     */
    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    /**
     * Helper method to generate public link from username
     */
    public void generatePublicLink(String username) {
        if (username != null && !username.isEmpty()) {
            this.publicLink = username.toLowerCase().replace(" ", "_");
        }
    }

    /**
     * Check if user allows feedback
     */
    public boolean allowsFeedback() {
        return this.receiveAnonymousFeedback || this.receiveNamedFeedback;
    }

    /**
     * Validate profile settings
     */
    public void validate() {
        if (!allowsFeedback()) {
            // User should receive at least one type of feedback
            // Or we can log a warning
        }
    }
}