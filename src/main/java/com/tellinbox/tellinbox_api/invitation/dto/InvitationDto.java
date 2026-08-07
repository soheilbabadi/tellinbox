package com.tellinbox.tellinbox_api.invitation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {
    private String token;
    private String inviteUrl;
    private boolean isActive;
    private Integer maxUses;
    private int currentUses;
}
