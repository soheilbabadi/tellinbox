package com.tellinbox.tellinbox_api.invitation.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.invitation.dto.InvitationDto;
import com.tellinbox.tellinbox_api.invitation.entity.Invitation;
import com.tellinbox.tellinbox_api.invitation.repository.InvitationRepository;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final MessageSource messageSource;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public InvitationDto createInvitation(UUID userId, Integer maxUses, Integer expiresInSeconds) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.user_not_found")));

        String token = generateSecureToken();
        
        Invitation invitation = Invitation.builder()
                .token(token)
                .user(user)
                .isActive(true)
                .maxUses(maxUses)
                .currentUses(0)
                .expiresAt(expiresInSeconds != null ? LocalDateTime.now().plusSeconds(expiresInSeconds) : null)
                .build();

        invitation = invitationRepository.save(invitation);

        return toDto(invitation);
    }

    @Override
    public InvitationDto updateInvitation(String token, Integer maxUses, Integer expiresInSeconds) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invitation_not_found")));

        if (maxUses != null) {
            invitation.setMaxUses(maxUses);
            // If increasing max uses and invitation was deactivated due to limit, reactivate it
            if (!invitation.isActive() && invitation.getCurrentUses() < maxUses) {
                invitation.setActive(true);
            }
        }

        if (expiresInSeconds != null) {
            invitation.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        } else if (expiresInSeconds == 0) {
            // Explicitly set to null to remove expiration
            invitation.setExpiresAt(null);
        }

        invitation = invitationRepository.save(invitation);
        return toDto(invitation);
    }

    @Transactional(readOnly = true)
    @Override
    public InvitationDto getInvitationByToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invitation_not_found")));
        return toDto(invitation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InvitationDto> getUserInvitations(UUID userId) {
        return invitationRepository.findAll().stream()
                .filter(inv -> inv.getUser().getId().equals(userId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invitation_not_found")));
        invitation.setActive(false);
        invitationRepository.save(invitation);
    }

    @Override
    public void activateInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invitation_not_found")));
        invitation.setActive(true);
        invitationRepository.save(invitation);
    }

    @Override
    public Invitation validateAndUseInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invalid_invitation_token")));

        if (!invitation.isActive()) {
            throw new ValidationException(getMessage("error.ValidationException.invitation_is_no_longer_active"));
        }

        if (invitation.getExpiresAt() != null && LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            throw new ValidationException(getMessage("error.ValidationException.invitation_has_expired"));
        }

        if (invitation.getMaxUses() != null && invitation.getCurrentUses() >= invitation.getMaxUses()) {
            throw new ValidationException(getMessage("error.ValidationException.invitation_has_reached_maximum_uses"));
        }

        invitation.setCurrentUses(invitation.getCurrentUses() + 1);
        
        if (invitation.getMaxUses() != null && invitation.getCurrentUses() >= invitation.getMaxUses()) {
            invitation.setActive(false);
        }

        return invitationRepository.save(invitation);
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private InvitationDto toDto(Invitation invitation) {
        return InvitationDto.builder()
                .token(invitation.getToken())
                .inviteUrl(baseUrl + "/comment/" + invitation.getToken())
                .isActive(invitation.isActive())
                .maxUses(invitation.getMaxUses())
                .currentUses(invitation.getCurrentUses())
                .build();
    }

    /**
     * Get localized message from messages.properties
     * @param key Message key
     * @param args Optional arguments for message formatting
     * @return Localized message
     */
    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
