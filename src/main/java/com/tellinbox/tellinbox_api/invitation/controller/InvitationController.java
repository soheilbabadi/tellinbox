package com.tellinbox.tellinbox_api.invitation.controller;

import com.tellinbox.tellinbox_api.invitation.dto.InvitationDto;
import com.tellinbox.tellinbox_api.invitation.service.InvitationService;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UserRepository userRepository;

    /**
     * Create a new invitation for anonymous comments.
     * 
     * @param maxUses Maximum number of times the invitation can be used (null for unlimited)
     * @param expiresInSeconds Expiration time in seconds (null for no expiration, 0 to remove expiration)
     */
    @PostMapping
    public ResponseEntity<InvitationDto> createInvitation(
            @RequestParam(required = false) Integer maxUses,
            @RequestParam(required = false) Integer expiresInSeconds,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUsername(userDetails.getUsername());
        
        InvitationDto invitation = invitationService.createInvitation(userId, maxUses, expiresInSeconds);
        return new ResponseEntity<>(invitation, HttpStatus.CREATED);
    }

    /**
     * Update an existing invitation's settings.
     * 
     * @param token The invitation token
     * @param maxUses New maximum uses (optional)
     * @param expiresInSeconds New expiration time in seconds (optional, 0 to remove expiration)
     */
    @PutMapping("/{token}")
    public ResponseEntity<InvitationDto> updateInvitation(
            @PathVariable String token,
            @RequestParam(required = false) Integer maxUses,
            @RequestParam(required = false) Integer expiresInSeconds) {
        
        InvitationDto invitation = invitationService.updateInvitation(token, maxUses, expiresInSeconds);
        return new ResponseEntity<>(invitation, HttpStatus.OK);
    }

    /**
     * Get all invitations for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<InvitationDto>> getUserInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUsername(userDetails.getUsername());
        List<InvitationDto> invitations = invitationService.getUserInvitations(userId);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    /**
     * Get a specific invitation by token.
     */
    @GetMapping("/{token}")
    public ResponseEntity<InvitationDto> getInvitation(@PathVariable String token) {
        InvitationDto invitation = invitationService.getInvitationByToken(token);
        return new ResponseEntity<>(invitation, HttpStatus.OK);
    }

    /**
     * Deactivate an invitation (make it unusable).
     */
    @PostMapping("/{token}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateInvitation(@PathVariable String token) {
        invitationService.deactivateInvitation(token);
        return new ResponseEntity<>(Map.of("message", "Invitation deactivated successfully"), HttpStatus.OK);
    }

    /**
     * Activate a previously deactivated invitation.
     */
    @PostMapping("/{token}/activate")
    public ResponseEntity<Map<String, String>> activateInvitation(@PathVariable String token) {
        invitationService.activateInvitation(token);
        return new ResponseEntity<>(Map.of("message", "Invitation activated successfully"), HttpStatus.OK);
    }

    /**
     * Helper method to extract user ID from username.
     */
    private Long getUserIdFromUsername(String username) {
        return userRepository.findByMobile(username)
                .or(() -> userRepository.findByEmail(username))
                .or(() -> userRepository.findByUsername(username))
                .map(UserModel::getId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + username));
    }
}
