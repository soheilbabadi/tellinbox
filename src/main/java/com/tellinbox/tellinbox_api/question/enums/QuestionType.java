package com.tellinbox.tellinbox_api.question.enums;

import lombok.Getter;

@Getter
public enum QuestionType {
    MULTIPLE_CHOICE("چند گزینه‌ای"),
    TRUE_FALSE("صحیح/غلط"),
    SHORT_ANSWER("پاسخ کوتاه"),
    LONG_ANSWER("پاسخ تشریحی"),
    RATING("امتیازدهی"),
    LIKERT_SCALE("مقیاس لیکرت"),
    YES_NO("بله/خیر"),
    DATE("تاریخ"),
    NUMBER("عدد");

    private final String persianName;

    QuestionType(String persianName) {
        this.persianName = persianName;
    }
}
