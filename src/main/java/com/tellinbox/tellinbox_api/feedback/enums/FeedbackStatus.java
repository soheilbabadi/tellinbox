package com.tellinbox.tellinbox_api.feedback.enums;

import lombok.Getter;

@Getter
public enum FeedbackStatus {
    PENDING("در انتظار بررسی"),
    PUBLISHED("منتشر شده"),
    ARCHIVED("بایگانی شده"),
    DELETED("حذف شده"),
    FLAGGED("علامت‌گذاری شده"),
    REPORTED("گزارش شده");

    private final String persianName;

    FeedbackStatus(String persianName) {
        this.persianName = persianName;
    }

    public boolean isActive() {
        return this == PUBLISHED;
    }

    public boolean isModifiable() {
        return this == PENDING || this == PUBLISHED;
    }
}