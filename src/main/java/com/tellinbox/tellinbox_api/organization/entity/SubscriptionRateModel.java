package com.tellinbox.tellinbox_api.organization.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing subscription pricing/rate plans.
 * Defines the price for each combination of subscription plan and billing cycle.
 */
@Entity
@Table(
    name = "subscription_rates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sub_rate_plan_cycle", columnNames = {"plan", "billing_cycle"})
    },
    indexes = {
        @Index(name = "idx_sub_rate_plan", columnList = "plan"),
        @Index(name = "idx_sub_rate_billing_cycle", columnList = "billing_cycle"),
        @Index(name = "idx_sub_rate_is_active", columnList = "isActive")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class SubscriptionRateModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Subscription plan (FREE, BASIC, PREMIUM, ENTERPRISE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan;

    /**
     * Billing cycle (MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private com.tellinbox.tellinbox_api.organization.enums.BillingCycle billingCycle;

    /**
     * Base price for this plan and billing cycle
     */
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    /**
     * Discount percentage (0-100)
     */
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    /**
     * Final price after discount
     */
    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    /**
     * Currency (e.g., IRR, USD)
     */
    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "IRR";

    /**
     * Whether this rate is currently active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Maximum number of seats included in base price
     */
    @Column(name = "included_seats")
    @Builder.Default
    private Integer includedSeats = 1;

    /**
     * Price per additional seat
     */
    @Column(name = "additional_seat_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal additionalSeatPrice = BigDecimal.ZERO;

    /**
     * Description of features included in this rate
     */
    @Column(name = "features", length = 2000)
    private String features;

    /**
     * Whether this rate is recommended/popular
     */
    @Column(name = "is_recommended")
    @Builder.Default
    private Boolean isRecommended = false;

    /**
     * Display order for UI
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Valid from date
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * Valid until date
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @PrePersist
    @PreUpdate
    protected void calculateFinalPrice() {
        if (this.basePrice != null && this.discountPercentage != null) {
            this.finalPrice = this.basePrice.multiply(
                BigDecimal.ONE.subtract(this.discountPercentage.divide(BigDecimal.valueOf(100)))
            );
        } else if (this.basePrice != null) {
            this.finalPrice = this.basePrice;
        }
    }

    /**
     * Check if this rate is currently valid
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return this.isActive &&
               (this.validFrom == null || !now.isBefore(this.validFrom)) &&
               (this.validUntil == null || !now.isAfter(this.validUntil));
    }

    /**
     * Calculate price for given number of seats
     */
    public BigDecimal calculatePriceForSeats(int seats) {
        if (seats <= this.includedSeats) {
            return this.finalPrice;
        }
        
        int additionalSeats = seats - this.includedSeats;
        return this.finalPrice.add(
            this.additionalSeatPrice.multiply(BigDecimal.valueOf(additionalSeats))
        );
    }
}
