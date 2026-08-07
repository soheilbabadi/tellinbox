package com.tellinbox.tellinbox_api.user.enums;

import lombok.Getter;

/**
 * User account status
 */
@Getter
public enum UserStatus {
    ACTIVE("فعال"),
    INACTIVE("غیرفعال"),
    SUSPENDED("مسدود"),
    DELETED("حذف شده"),
    BANNED("ممنوع");

    private final String persianName;

    UserStatus(String persianName) {
        this.persianName = persianName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
