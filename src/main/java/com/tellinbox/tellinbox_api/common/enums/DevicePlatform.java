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
}
