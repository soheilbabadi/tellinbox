package com.tellinbox.tellinbox_api.user.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * Service implementation for profile picture operations using MinIO.
 * Provides business logic for uploading, deleting, and retrieving user profile pictures.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePictureServiceImpl implements ProfilePictureService {

    private final MessageSource messageSource;
    private final MinioClient minioClient;
    private final UserRepository userRepository;

    @Value("${app.minio.bucket-name:profile-pictures}")
    private String bucketName;

    @Override
    @Transactional
    @CacheEvict(value = "profilePictures", key = "#userId")
    public String uploadProfilePicture(UUID userId, MultipartFile file) {
        log.info("Uploading profile picture for user ID: {}", userId);

        try {
            // Validate file
            validateFile(file);

            // Find user
            UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.user_not_found")));

            // Ensure bucket exists
            ensureBucketExists();

            // Generate unique filename
            String fileName = generateFileName(userId, file.getOriginalFilename());

            // Upload file to MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // Generate URL
            String fileUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );

            // Update user's profile picture URL
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            log.info("Profile picture uploaded successfully for user ID: {}. URL: {}", userId, fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("Failed to upload profile picture for user ID: {}", userId, e);
            throw new TellInboxCustomException.ApplicationServerException(getMessage("error.ApplicationServerException.profile_image_upload_error") + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "profilePictures", key = "#userId")
    public void deleteProfilePicture(UUID userId) {
        log.info("Deleting profile picture for user ID: {}", userId);

        try {
            // Find user
            UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.user_not_found")));

            String currentProfilePictureUrl = user.getProfilePictureUrl();
            
            if (currentProfilePictureUrl != null && !currentProfilePictureUrl.isEmpty()) {
                // Extract object name from URL
                String objectName = extractObjectNameFromUrl(currentProfilePictureUrl);
                
                if (objectName != null) {
                    // Delete from MinIO
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
                    );
                    
                    log.info("Profile picture deleted from MinIO: {}", objectName);
                }

                // Clear user's profile picture URL
                user.setProfilePictureUrl(null);
                userRepository.save(user);

                log.info("Profile picture deleted successfully for user ID: {}", userId);
            } else {
                log.debug("No profile picture to delete for user ID: {}", userId);
            }

        } catch (Exception e) {
            log.error("Failed to delete profile picture for user ID: {}", userId, e);
            throw new TellInboxCustomException.ApplicationServerException(getMessage("error.ApplicationServerException.profile_image_delete_error") + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getProfilePictureUrl(UUID userId) {
        log.debug("Getting profile picture URL for user ID: {}", userId);

        try {
            UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.user_not_found")));

            String profilePictureUrl = user.getProfilePictureUrl();
            
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                // Check if the URL is a presigned URL that might have expired
                // If needed, generate a new presigned URL here
                return profilePictureUrl;
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to get profile picture URL for user ID: {}", userId, e);
            throw new TellInboxCustomException.ApplicationServerException(getMessage("error.ApplicationServerException.profile_image_link_error") + e.getMessage());
        }
    }

    /**
     * Validate the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
             throw new  TellInboxCustomException.ValidationException(getMessage("error.ValidationException.image_file_empty"));
        }

        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
             throw new  TellInboxCustomException.ValidationException(getMessage("error.ValidationException.file_size_exceeds_5mb"));
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
             throw new  TellInboxCustomException.ValidationException(getMessage("error.ValidationException.file_must_be_image"));
        }
    }

    /**
     * Check if the content type is a valid image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Generate a unique filename for the profile picture
     */
    private String generateFileName(UUID userId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return "users/" + userId.toString() + "/" + UUID.randomUUID().toString() + extension;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Extract object name from MinIO URL
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // URL format: http://minio:9000/bucket-name/object-name?X-Amz-...
            if (url.contains("/" + bucketName + "/")) {
                String pathAfterBucket = url.substring(url.indexOf("/" + bucketName + "/") + bucketName.length() + 2);
                // Remove query parameters
                if (pathAfterBucket.contains("?")) {
                    return pathAfterBucket.substring(0, pathAfterBucket.indexOf("?"));
                }
                return pathAfterBucket;
            }
        } catch (Exception e) {
            log.warn("Failed to extract object name from URL: {}", url, e);
        }
        return null;
    }

    /**
     * Ensure the bucket exists, create if not
     */
    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!bucketExists) {
            log.info("Creating bucket: {}", bucketName);
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }
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
