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
}
