package com.tellinbox.tellinbox_api.invitation.service;

import com.tellinbox.tellinbox_api.invitation.dto.InvitationDto;
import com.tellinbox.tellinbox_api.invitation.entity.Invitation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface InvitationService {
	InvitationDto createInvitation(UUID userId, Integer maxUses, Integer expiresInSeconds);

	InvitationDto updateInvitation(String token, Integer maxUses, Integer expiresInSeconds);

	@Transactional(readOnly = true)
	InvitationDto getInvitationByToken(String token);

	@Transactional(readOnly = true)
	List<InvitationDto> getUserInvitations(UUID userId);

	void deactivateInvitation(String token);

	void activateInvitation(String token);

	Invitation validateAndUseInvitation(String token);
}
