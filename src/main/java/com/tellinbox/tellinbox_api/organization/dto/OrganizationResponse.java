package com.tellinbox.tellinbox_api.organization.dto;

import com.tellinbox.tellinbox_api.organization.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for organization response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String registrationNumber;
    private OrganizationType type;
    private String description;
    private String logoUrl;
    private String website;
    private String phone;
    private String address;
    private UUID ownerId;
    private String ownerName;
    private Integer membersCount;
    private Boolean hasActiveSubscription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
