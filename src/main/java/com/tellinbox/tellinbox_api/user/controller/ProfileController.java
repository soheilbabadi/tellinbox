package com.tellinbox.tellinbox_api.user.controller;

import com.tellinbox.tellinbox_api.dto.ProfilePictureDto;
import com.tellinbox.tellinbox_api.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
    public ResponseEntity<ProfilePictureDto> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        ProfilePictureDto result = profilePictureService.uploadProfilePicture(username, file);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Delete the current profile picture for the authenticated user.
     */
    @DeleteMapping("/picture")
    public ResponseEntity<Map<String, String>> deleteProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        profilePictureService.deleteProfilePicture(username);
        
        return new ResponseEntity<>(Map.of("message", "Profile picture deleted successfully"), HttpStatus.OK);
    }

    /**
     * Get the current profile picture URL for the authenticated user.
     */
    @GetMapping("/picture")
    public ResponseEntity<ProfilePictureDto> getProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        ProfilePictureDto result = profilePictureService.getProfilePictureUrl(username);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
