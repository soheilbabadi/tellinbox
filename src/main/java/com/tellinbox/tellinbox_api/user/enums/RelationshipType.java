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

    public static RelationshipType fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (RelationshipType item : RelationshipType.values()) {
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