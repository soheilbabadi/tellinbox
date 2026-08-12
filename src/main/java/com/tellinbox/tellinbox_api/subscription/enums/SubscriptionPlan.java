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
}
