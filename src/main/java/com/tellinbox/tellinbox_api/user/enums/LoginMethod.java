package com.tellinbox.tellinbox_api.user.enums;

import lombok.Getter;

/**
 * روش‌های ورود کاربر به سیستم
 */
@Getter
public enum LoginMethod {
    PASSWORD("رمز عبور"),
    OTP("کد یکبار مصرف"),
    GOOGLE("حساب گوگل");

    private final String persianName;

    LoginMethod(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }

    public static LoginMethod fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (LoginMethod item : LoginMethod.values()) {
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
