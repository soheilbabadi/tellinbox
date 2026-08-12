package com.tellinbox.tellinbox_api.user.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for profile picture operations.
 * Defines the contract for uploading and managing user profile pictures using MinIO.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
public interface ProfilePictureService {

    /**
     * Upload a profile picture for a user
     * @param userId User UUID
     * @param file Profile picture file
     * @return URL of the uploaded picture
     */
    String uploadProfilePicture(UUID userId, MultipartFile file);

    /**
     * Delete a user's profile picture
     * @param userId User UUID
     */
    void deleteProfilePicture(UUID userId);

    /**
     * Get the profile picture URL for a user
     * @param userId User UUID
     * @return Profile picture URL or null if not set
     */
    @Cacheable(value = "profilePictures", key = "#userId")
    String getProfilePictureUrl(UUID userId);
}
