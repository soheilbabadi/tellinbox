package com.tellinbox.tellinbox_api.feedback.enums;

import lombok.Getter;

@Getter
public enum FeedbackCategoryType {
    HONESTY("صداقت"),
    RESPONSIBILITY("مسئولیت‌پذیری"),
    PUNCTUALITY("خوش‌قولی"),
    COMMUNICATION_SKILLS("مهارت ارتباطی"),
    COLLABORATION("همکاری"),
    PROFESSIONAL_ETHICS("اخلاق حرفه‌ای"),
    MANAGEMENT("مدیریت"),
    SOCIAL_BEHAVIOR("رفتار اجتماعی");

    private final String persianName;

    FeedbackCategoryType(String persianName) {
        this.persianName = persianName;
    }

    public static FeedbackCategoryType fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (FeedbackCategoryType item : FeedbackCategoryType.values()) {
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
