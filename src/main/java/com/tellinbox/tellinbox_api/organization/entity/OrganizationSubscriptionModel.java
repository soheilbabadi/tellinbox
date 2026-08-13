package com.tellinbox.tellinbox_api.organization.entity;

import com.tellinbox.tellinbox_api.organization.enums.BillingCycle;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing an organization's subscription.
 * Tracks the subscription plan, billing cycle, status, and validity period.
 */
@Entity
@Table(
    name = "organization_subscriptions",
    indexes = {
        @Index(name = "idx_org_sub_organization_id", columnList = "organization_id"),
        @Index(name = "idx_org_sub_plan", columnList = "plan"),
        @Index(name = "idx_org_sub_status", columnList = "status"),
        @Index(name = "idx_org_sub_start_date", columnList = "startDate"),
        @Index(name = "idx_org_sub_end_date", columnList = "endDate")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class OrganizationSubscriptionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Reference to the organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrganizationModel organization;

    /**
     * Subscription plan (FREE, BASIC, PREMIUM, ENTERPRISE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private SubscriptionPlan plan;

    /**
     * Billing cycle (MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    /**
     * Subscription status (ACTIVE, INACTIVE, EXPIRED, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    /**
     * Price paid for this subscription
     */
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Currency (e.g., IRR, USD)
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "IRR";

    /**
     * Subscription start date
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Subscription end date
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Number of seats/licenses included in this subscription
     */
    @Column(name = "seats_count")
    @Builder.Default
    private Integer seatsCount = 1;

    /**
     * Whether auto-renewal is enabled
     */
    @Column(name = "auto_renewal")
    @Builder.Default
    private Boolean autoRenewal = false;

    /**
     * External payment reference ID
     */
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    /**
     * Notes about the subscription
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Activate the subscription
     */
    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        if (this.billingCycle != null) {
            this.endDate = this.startDate.plusMonths(this.billingCycle.getMonths());
        }
    }

    /**
     * Deactivate the subscription
     */
    public void deactivate() {
        this.status = SubscriptionStatus.INACTIVE;
    }

    /**
     * Mark as expired
     */
    public void markAsExpired() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /**
     * Check if subscription is currently active
     */
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE &&
               this.endDate != null &&
               this.endDate.isAfter(LocalDateTime.now());
    }

    /**
     * Check if subscription has expired
     */
    public boolean isExpired() {
        return this.endDate != null && this.endDate.isBefore(LocalDateTime.now());
    }
}
