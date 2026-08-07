package com.tellinbox.tellinbox_api.user.enums;

import lombok.Getter;

@Getter
public enum RelationshipType {
    FRIEND("دوست"),
    COLLEAGUE("همکار"),
    MANAGER("مدیر"),
    CUSTOMER("مشتری"),
    CLASSMATE("همکلاسی"),
    FAMILY("عضو خانواده"),
    PARTNER("شریک"),
    OTHER("سایر");

    private final String persianName;

    RelationshipType(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }
}