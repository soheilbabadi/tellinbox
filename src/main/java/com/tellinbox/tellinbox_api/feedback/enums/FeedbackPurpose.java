package com.tellinbox.tellinbox_api.feedback.enums;

import lombok.Getter;

@Getter
public enum FeedbackPurpose {
    JOB("استخدام"),
    FRIENDSHIP("دوستی"),
    PERSONAL_GROWTH("توسعه فردی"),
    TEAM("تیم کاری"),
    CUSTOMER("مشتری"),
    UNIVERSITY("دانشگاه"),
    COLLABORATION("همکاری"),
    OTHER("سایر");

    private final String persianName;

    FeedbackPurpose(String persianName) {
        this.persianName = persianName;
    }

}