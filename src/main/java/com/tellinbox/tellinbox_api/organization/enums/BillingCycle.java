package com.tellinbox.tellinbox_api.organization.enums;

import lombok.Getter;

/**
 * Enum representing the billing cycle for subscription plans
 */
@Getter
public enum BillingCycle {
    MONTHLY("ماهانه", 1),
    QUARTERLY("سه ماهه", 3),
    SEMI_ANNUAL("شش ماهه", 6),
    ANNUAL("یکساله", 12);

    private final String persianName;
    private final int months;

    BillingCycle(String persianName, int months) {
        this.persianName = persianName;
        this.months = months;
    }

    public static BillingCycle fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (BillingCycle item : BillingCycle.values()) {
            if (item.name().equalsIgnoreCase(normalizedInput)) {
                return item;
            }
            
            if (item.getPersianName() != null && item.getPersianName().equals(normalizedInput)) {
                return item;
            }
        }
        
        return null;
    }
}
