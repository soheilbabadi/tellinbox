package com.tellinbox.tellinbox_api.user.controller;

import com.tellinbox.tellinbox_api.security.CustomUserDetails;
import com.tellinbox.tellinbox_api.user.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfilePictureService profilePictureService;

    /**
     * Upload a new profile picture for the authenticated user.
     * Replaces the existing picture if one exists.
     */
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Get userId from CustomUserDetails (UUID type)
        UUID userId = null;
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            userId = customUserDetails.getUserId();
        }
        
        UUID username = UUID.fromString(userDetails.getUsername());
        ProfilePictureDto result = profilePictureService.uploadProfilePicture(username, file);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Delete the current profile picture for the authenticated user.
     */
    @DeleteMapping("/picture")
    public ResponseEntity<Map<String, String>> deleteProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Get userId from CustomUserDetails (UUID type)
        UUID userId = null;
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            userId = customUserDetails.getUserId();
        }
        
        String username = userDetails.getUsername();
        profilePictureService.deleteProfilePicture(UUID.fromString(username));
        
        return new ResponseEntity<>(Map.of("message", "Profile picture deleted successfully"), HttpStatus.OK);
    }

    /**
     * Get the current profile picture URL for the authenticated user.
     */
    @GetMapping("/picture")
    public ResponseEntity<ProfilePictureDto> getProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Get userId from CustomUserDetails (UUID type)
        UUID userId = null;
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            userId = customUserDetails.getUserId();
        }
        
        String username = userDetails.getUsername();
        ProfilePictureDto result = profilePictureService.getProfilePictureUrl(UUID.fromString(username));
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
