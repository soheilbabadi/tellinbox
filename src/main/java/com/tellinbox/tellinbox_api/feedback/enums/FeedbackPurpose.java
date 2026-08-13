package com.tellinbox.tellinbox_api.feedback.enums;

import lombok.Getter;

@Getter
public enum FeedbackPurpose {
    JOB("استخدام"),
    FRIENDSHIP("دوستی"),
    PERSONAL_GROWTH("توسعه فردی"),
    TEAM("تیم کاری"),
    CUSTOMER("مشتری"),
    UNIVERSITY("دانشگاه"),
    COLLABORATION("همکاری"),
    OTHER("سایر");

    private final String persianName;

    FeedbackPurpose(String persianName) {
        this.persianName = persianName;
    }

    public static FeedbackPurpose fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (FeedbackPurpose item : FeedbackPurpose.values()) {
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