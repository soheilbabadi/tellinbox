package com.tellinbox.tellinbox_api.feedback.enums;

import lombok.Getter;

@Getter
public enum FeedbackVisibility {
    PRIVATE("خصوصی"),
    SHARED("اشتراک‌گذاری شده"),
    PUBLIC("عمومی");

    private final String persianName;

    FeedbackVisibility(String persianName) {
        this.persianName = persianName;
    }

}