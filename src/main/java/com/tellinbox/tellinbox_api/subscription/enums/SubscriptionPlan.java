package com.tellinbox.tellinbox_api.subscription.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    FREE("رایگان"),
    BASIC("پایه"),
    PREMIUM("پریمیوم"),
    ENTERPRISE("سازمانی");

    private final String persianName;

    SubscriptionPlan(String persianName) {
        this.persianName = persianName;
    }

    public static SubscriptionPlan fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (SubscriptionPlan item : SubscriptionPlan.values()) {
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
