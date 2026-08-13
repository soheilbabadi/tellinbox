package com.tellinbox.tellinbox_api.organization.dto;

import com.tellinbox.tellinbox_api.organization.enums.BillingCycle;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for purchasing a subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseSubscriptionRequest {

    @NotNull(message = "پلن اشتراک الزامی است")
    private SubscriptionPlan plan;

    @NotNull(message = "چرخه صورتحساب الزامی است")
    private BillingCycle billingCycle;

    @Min(value = 1, message = "تعداد صندلی باید حداقل ۱ باشد")
    @Builder.Default
    private Integer seatsCount = 1;

    private Boolean autoRenewal = false;

    private String notes;
}
