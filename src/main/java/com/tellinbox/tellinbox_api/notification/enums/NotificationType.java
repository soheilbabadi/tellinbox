package com.tellinbox.tellinbox_api.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    FEEDBACK_RECEIVED("دریافت بازخورد جدید"),
    FEEDBACK_RESPONSE("پاسخ به بازخورد"),
    FRIEND_REQUEST("درخواست دوستی"),
    FRIEND_REQUEST_ACCEPTED("پذیرش درخواست دوستی"),
    SUBSCRIPTION_EXPIRED("انقضای اشتراک"),
    SUBSCRIPTION_RENEWED("تمدید اشتراک"),
    PAYMENT_SUCCESS("پرداخت موفق"),
    PAYMENT_FAILED("پرداخت ناموفق"),
    REPORT_PROCESSED("بررسی گزارش"),
    SYSTEM_MESSAGE("پیام سیستمی"),
    PROFILE_VIEW("بازدید پروفایل"),
    REMINDER("یادآور");

    private final String persianName;

    NotificationType(String persianName) {
        this.persianName = persianName;
    }
}
