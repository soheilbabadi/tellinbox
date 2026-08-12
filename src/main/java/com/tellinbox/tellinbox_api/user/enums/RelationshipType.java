package com.tellinbox.tellinbox_api.user.enums;

import lombok.Getter;

/**
 * نسبت نویسنده بازخورد با گیرنده
 */
@Getter
public enum RelationshipType {
    FRIEND("دوست"),
    COLLEAGUE("همکار"),
    MANAGER("مدیر"),
    CUSTOMER("مشتری"),
    CLASSMATE("همکلاسی"),
    FAMILY_MEMBER("عضو خانواده"),
    ANONYMOUS("ناشناس");

    private final String persianName;

    RelationshipType(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }
}