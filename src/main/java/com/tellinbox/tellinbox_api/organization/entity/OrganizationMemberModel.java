package com.tellinbox.tellinbox_api.organization.entity;

import com.tellinbox.tellinbox_api.user.model.UserModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing a member of an organization.
 * Members are invited via invitation tokens and can have different roles.
 */
@Entity
@Table(
    name = "organization_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_org_member_org_user", columnNames = {"organization_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_org_member_organization_id", columnList = "organization_id"),
        @Index(name = "idx_org_member_user_id", columnList = "user_id"),
        @Index(name = "idx_org_member_joined_at", columnList = "joinedAt")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class OrganizationMemberModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /**
     * Reference to the organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrganizationModel organization;

    /**
     * Reference to the user who is a member
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserModel user;

    /**
     * Member role in the organization (ADMIN, MEMBER, VIEWER, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    /**
     * Whether the member is active in the organization
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Invitation token that was used to join
     */
    @Column(name = "invitation_token", length = 64)
    private String invitationToken;

    /**
     * Member status enum
     */
    public enum MemberRole {
        OWNER,      // Organization owner
        ADMIN,      // Administrator with full access
        MANAGER,    // Can manage members and subscriptions
        MEMBER,     // Regular member
        VIEWER      // Read-only access
    }

    /**
     * Deactivate member
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate member
     */
    public void activate() {
        this.isActive = true;
    }
}
