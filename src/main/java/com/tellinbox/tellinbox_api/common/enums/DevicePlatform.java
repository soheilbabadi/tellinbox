package com.tellinbox.tellinbox_api.common.enums;

import lombok.Getter;

@Getter
public enum DevicePlatform {
    ANDROID("اندروید"),
    IOS("iOS"),
    WEB("وب"),
    DESKTOP("دسکتاپ"),
    OTHER("سایر");

    private final String persianName;

    DevicePlatform(String persianName) {
        this.persianName = persianName;
    }

    public static DevicePlatform fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (DevicePlatform item : DevicePlatform.values()) {
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
