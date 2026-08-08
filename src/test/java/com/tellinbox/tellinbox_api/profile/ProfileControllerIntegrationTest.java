package com.tellinbox.tellinbox_api.profile;

import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // "password"
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void uploadProfilePicture_Success() throws Exception {
        // Create a mock image file
        String imageContent = "fake-image-content";
        MockMultipartFile imageFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/profile/picture")
                .file(imageFile)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void uploadProfilePicture_InvalidFileType() throws Exception {
        // Create a mock text file (not allowed)
        MockMultipartFile textFile = new MockMultipartFile(
            "file",
            "document.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "text content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/profile/picture")
                .file(textFile)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProfilePictureUrl_Success() throws Exception {
        // First upload a picture
        String imageContent = "fake-image-content";
        MockMultipartFile imageFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/profile/picture")
                .file(imageFile)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then retrieve the URL
        mockMvc.perform(get("/api/v1/profile/picture"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteProfilePicture_Success() throws Exception {
        // First upload a picture
        String imageContent = "fake-image-content";
        MockMultipartFile imageFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            imageContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/profile/picture")
                .file(imageFile)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then delete it
        mockMvc.perform(delete("/api/v1/profile/picture")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify it's deleted
        mockMvc.perform(get("/api/v1/profile/picture"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void uploadProfilePicture_Unauthenticated() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/profile/picture")
                .file(imageFile)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }
}
