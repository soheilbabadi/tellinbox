package com.tellinbox.tellinbox_api.questionnaire.entity;

import com.tellinbox.tellinbox_api.organization.entity.OrganizationModel;
import com.tellinbox.tellinbox_api.questionnaire.enums.QuestionnaireStatus;
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
 * Entity class representing a Questionnaire in the system.
 * A questionnaire belongs to an organization and contains multiple questions.
 */
@Entity
@Table(
    name = "questionnaires",
    indexes = {
        @Index(name = "idx_questionnaires_organization_id", columnList = "organization_id"),
        @Index(name = "idx_questionnaires_created_by", columnList = "created_by"),
        @Index(name = "idx_questionnaires_status", columnList = "status"),
        @Index(name = "idx_questionnaires_is_active", columnList = "is_active"),
        @Index(name = "idx_questionnaires_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"questions", "responses", "organization", "owner"})
public class QuestionnaireModel {

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
     * Title of the questionnaire
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Description of the questionnaire
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Organization that owns this questionnaire
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questionnaires_organization"))
    private OrganizationModel organization;

    /**
     * User who created this questionnaire (must be organization manager/admin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questionnaires_owner"))
    private UserModel owner;

    /**
     * Status of the questionnaire (DRAFT, PUBLISHED, CLOSED, ARCHIVED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private QuestionnaireStatus status = QuestionnaireStatus.DRAFT;

    /**
     * Whether the questionnaire is currently active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Start date for accepting responses
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * End date for accepting responses
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Whether respondents can submit anonymously
     */
    @Column(name = "allow_anonymous", nullable = false)
    @Builder.Default
    private Boolean allowAnonymous = false;

    /**
     * Maximum number of responses allowed (null = unlimited)
     */
    @Column(name = "max_responses")
    private Integer maxResponses;

    /**
     * Current number of responses
     */
    @Column(name = "response_count", nullable = false)
    @Builder.Default
    private Integer responseCount = 0;

    /**
     * Whether to show results to respondents after submission
     */
    @Column(name = "show_results", nullable = false)
    @Builder.Default
    private Boolean showResults = false;

    /**
     * Welcome message shown before starting the questionnaire
     */
    @Column(name = "welcome_message", length = 1000)
    private String welcomeMessage;

    /**
     * Thank you message shown after completion
     */
    @Column(name = "thank_you_message", length = 1000)
    private String thankYouMessage;

    // ==================== Relationships ====================

    /**
     * Questions in this questionnaire
     */
    @OneToMany(
        mappedBy = "questionnaire",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("order ASC")
    @Builder.Default
    private List<QuestionModel> questions = new ArrayList<>();

    /**
     * Responses to this questionnaire
     */
    @OneToMany(
        mappedBy = "questionnaire",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<QuestionnaireResponseModel> responses = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Add a question to this questionnaire
     */
    public void addQuestion(QuestionModel question) {
        question.setQuestionnaire(this);
        this.questions.add(question);
    }

    /**
     * Remove a question from this questionnaire
     */
    public void removeQuestion(QuestionModel question) {
        this.questions.remove(question);
        question.setQuestionnaire(null);
    }

    /**
     * Increment response count
     */
    public void incrementResponseCount() {
        this.responseCount = (this.responseCount == null ? 0 : this.responseCount) + 1;
    }

    /**
     * Check if questionnaire accepts responses
     */
    public boolean acceptsResponses() {
        if (!Boolean.TRUE.equals(this.isActive)) {
            return false;
        }
        if (this.status != QuestionnaireStatus.PUBLISHED) {
            return false;
        }
        if (this.startDate != null && LocalDateTime.now().isBefore(this.startDate)) {
            return false;
        }
        if (this.endDate != null && LocalDateTime.now().isAfter(this.endDate)) {
            return false;
        }
        if (this.maxResponses != null && this.responseCount >= this.maxResponses) {
            return false;
        }
        return true;
    }

    /**
     * Publish the questionnaire
     */
    public void publish() {
        this.status = QuestionnaireStatus.PUBLISHED;
        this.isActive = true;
        if (this.startDate == null) {
            this.startDate = LocalDateTime.now();
        }
    }

    /**
     * Close the questionnaire
     */
    public void close() {
        this.status = QuestionnaireStatus.CLOSED;
        this.endDate = LocalDateTime.now();
    }

    /**
     * Archive the questionnaire
     */
    public void archive() {
        this.status = QuestionnaireStatus.ARCHIVED;
        this.isActive = false;
    }

    /**
     * Soft delete the questionnaire
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
        this.isActive = false;
    }

    /**
     * Restore deleted questionnaire
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if questionnaire is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted) && Boolean.TRUE.equals(this.isActive);
    }
}
