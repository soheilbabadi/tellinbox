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

    public static FeedbackVisibility fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (FeedbackVisibility item : FeedbackVisibility.values()) {
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