package com.tellinbox.tellinbox_api.report.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("در انتظار بررسی"),
    UNDER_REVIEW("در حال بررسی"),
    RESOLVED("حل شده"),
    REJECTED("رد شده"),
    ESCALATED("ارجاع داده شده"),
    CLOSED("بسته شده");

    private final String persianName;

    ReportStatus(String persianName) {
        this.persianName = persianName;
    }

    public boolean isProcessed() {
        return this == RESOLVED || this == REJECTED || this == CLOSED;
    }

    public static ReportStatus fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (ReportStatus item : ReportStatus.values()) {
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
