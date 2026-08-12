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
}
