package com.tellinbox.tellinbox_api.organization.dto;

import com.tellinbox.tellinbox_api.organization.enums.BillingCycle;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for subscription response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private BigDecimal price;
    private String currency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer seatsCount;
    private Boolean autoRenewal;
    private String paymentReference;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
