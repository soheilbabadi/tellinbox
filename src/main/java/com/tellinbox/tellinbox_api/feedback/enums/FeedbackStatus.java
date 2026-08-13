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

    public static FeedbackStatus fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (FeedbackStatus item : FeedbackStatus.values()) {
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