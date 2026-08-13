package com.tellinbox.tellinbox_api.subscription.enums;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    ACTIVE("فعال"),
    INACTIVE("غیرفعال"),
    EXPIRED("منقضی شده"),
    PENDING("در انتظار"),
    CANCELLED("لغو شده"),
    SUSPENDED("معلق");

    private final String persianName;

    SubscriptionStatus(String persianName) {
        this.persianName = persianName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public static SubscriptionStatus fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (SubscriptionStatus item : SubscriptionStatus.values()) {
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
