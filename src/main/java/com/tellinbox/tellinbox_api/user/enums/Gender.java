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

}