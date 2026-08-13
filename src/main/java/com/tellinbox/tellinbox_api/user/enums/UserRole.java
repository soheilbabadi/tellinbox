package com.tellinbox.tellinbox_api.user.enums;

public enum UserRole {
    ADMIN("Admin", "Full system access"),
    MODERATOR("Moderator", "Can manage content and users"),
    USER("User", "Standard user access"),
    GUEST("Guest", "Limited read-only access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        
        String normalizedInput = input.trim();

        for (UserRole item : UserRole.values()) {
            if (item.name().equalsIgnoreCase(normalizedInput)) {
                return item;
            }
            
            if (item.getDisplayName() != null && item.getDisplayName().equalsIgnoreCase(normalizedInput)) {
                return item;
            }
        }
        
        return null;
    }
}