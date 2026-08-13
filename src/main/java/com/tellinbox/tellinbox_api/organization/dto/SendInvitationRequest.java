package com.tellinbox.tellinbox_api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for sending organization invitation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInvitationRequest {

    private UUID organizationId;

    /**
     * Email of the user to invite
     */
    private String email;

    /**
     * Optional message to include in invitation
     */
    private String message;
}
