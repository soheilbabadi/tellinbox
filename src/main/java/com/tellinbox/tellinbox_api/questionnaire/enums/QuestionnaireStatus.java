package com.tellinbox.tellinbox_api.questionnaire.enums;

import lombok.Getter;

/**
 * Status of a questionnaire
 */
@Getter
public enum QuestionnaireStatus {
    DRAFT("پیش‌نویس", "Questionnaire is being created and not yet published"),
    PUBLISHED("منتشر شده", "Questionnaire is active and accepting responses"),
    CLOSED("بسته شده", "Questionnaire is closed and no longer accepting responses"),
    ARCHIVED("بایگانی شده", "Questionnaire is archived for historical reference");

    private final String persianName;
    private final String description;

    QuestionnaireStatus(String persianName, String description) {
        this.persianName = persianName;
        this.description = description;
    }

    public static QuestionnaireStatus fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (QuestionnaireStatus item : QuestionnaireStatus.values()) {
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
