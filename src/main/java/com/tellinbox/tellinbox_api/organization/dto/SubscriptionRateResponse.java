package com.tellinbox.tellinbox_api.organization.dto;

import com.tellinbox.tellinbox_api.organization.enums.BillingCycle;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for subscription rate response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRateResponse {

    private UUID id;
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
    private BigDecimal basePrice;
    private BigDecimal discountPercentage;
    private BigDecimal finalPrice;
    private String currency;
    private Integer includedSeats;
    private BigDecimal additionalSeatPrice;
    private String features;
    private Boolean isRecommended;
    private Integer displayOrder;
    private Boolean isValid;
}
