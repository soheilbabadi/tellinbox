package com.tellinbox.tellinbox_api.user.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("مرد"),
    FEMALE("زن"),
    OTHER("سایر"),
    PREFER_NOT_TO_SAY("ترجیح می‌دهم نگویم");

    private final String persianName;

    Gender(String persianName) {
        this.persianName = persianName;
    }

    public static Gender fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (Gender item : Gender.values()) {
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