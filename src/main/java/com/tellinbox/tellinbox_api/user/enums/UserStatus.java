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

    public static UserStatus fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (UserStatus item : UserStatus.values()) {
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
