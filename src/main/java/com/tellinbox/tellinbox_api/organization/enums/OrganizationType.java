package com.tellinbox.tellinbox_api.organization.enums;

import lombok.Getter;

/**
 * Enum representing the type of organization
 */
@Getter
public enum OrganizationType {
    COMPANY("شرکت"),
    STARTUP("استارتاپ"),
    INSTITUTION("موسسه"),
    GOVERNMENT("دولتی"),
    NON_PROFIT("غیرانتفاعی"),
    OTHER("سایر");

    private final String persianName;

    OrganizationType(String persianName) {
        this.persianName = persianName;
    }

    public static OrganizationType fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (OrganizationType item : OrganizationType.values()) {
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
