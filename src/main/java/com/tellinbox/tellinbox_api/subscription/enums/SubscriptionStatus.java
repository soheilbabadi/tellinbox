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
}
