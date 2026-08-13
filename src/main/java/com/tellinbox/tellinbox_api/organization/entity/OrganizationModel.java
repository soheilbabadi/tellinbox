package com.tellinbox.tellinbox_api.organization.entity;

import com.tellinbox.tellinbox_api.organization.enums.OrganizationType;
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
 * Entity class representing an Organization in the system.
 * Organizations can have subscriptions and members.
 */
@Entity
@Table(
    name = "organizations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_organizations_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_organizations_registration_number", columnNames = "registration_number")
    },
    indexes = {
        @Index(name = "idx_organizations_name", columnList = "name"),
        @Index(name = "idx_organizations_type", columnList = "type"),
        @Index(name = "idx_organizations_created_at", columnList = "createdAt"),
        @Index(name = "idx_organizations_owner_id", columnList = "owner_id")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class OrganizationModel {

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
     * Organization name - unique identifier
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Official registration number
     */
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    /**
     * Organization type (Company, Startup, Institution, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private OrganizationType type;

    /**
     * Organization description
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * URL to organization's logo (stored in MinIO)
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Organization website
     */
    @Column(name = "website", length = 255)
    private String website;

    /**
     * Phone number
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Physical address
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Owner/Founder of the organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserModel owner;

    /**
     * One-to-Many relationship with OrganizationMember
     */
    @OneToMany(
        mappedBy = "organization",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrganizationMemberModel> members = new ArrayList<>();

    /**
     * One-to-Many relationship with OrganizationSubscription
     */
    @OneToMany(
        mappedBy = "organization",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrganizationSubscriptionModel> subscriptions = new ArrayList<>();

    /**
     * Soft delete organization
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    /**
     * Restore deleted organization
     */
    public void restore() {
        this.deletedAt = null;
        this.isDeleted = false;
    }

    /**
     * Check if organization is active
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }
}
