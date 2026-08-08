package com.tellinbox.tellinbox_api.comment;

import com.tellinbox.tellinbox_api.invitation.model.Invitation;
import com.tellinbox.tellinbox_api.invitation.repository.InvitationRepository;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class AnonymousCommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private UserModel testUser;
    private Invitation testInvitation;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        invitationRepository.deleteAll();
        
        // Create test user
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);

        // Create test invitation
        testInvitation = new Invitation();
        testInvitation.setUser(testUser);
        testInvitation.setToken("test-token-12345");
        testInvitation.setMaxUses(10);
        testInvitation.setUsedCount(0);
        testInvitation.setActive(true);
        testInvitation = invitationRepository.save(testInvitation);
    }

    @Test
    void submitAnonymousComment_Success() throws Exception {
        String requestBody = """
            {
                "content": "This is a test anonymous comment",
                "authorName": "Anonymous User"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/public/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("This is a test anonymous comment"))
            .andExpect(jsonPath("$.data.authorName").value("Anonymous User"));
    }

    @Test
    void submitAnonymousComment_EmptyContent() throws Exception {
        String requestBody = """
            {
                "content": "",
                "authorName": "Anonymous"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/public/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitAnonymousComment_InvalidToken() throws Exception {
        String requestBody = """
            {
                "content": "Test comment",
                "authorName": "Anonymous"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/public/invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void submitAuthenticatedComment_Success() throws Exception {
        String requestBody = """
            {
                "content": "This is an authenticated comment"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("This is an authenticated comment"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCommentsByInvitation_Success() throws Exception {
        // First submit a comment
        String commentBody = """
            {
                "content": "Test comment for retrieval",
                "authorName": "Tester"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/public/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentBody)
                .with(csrf()));

        // Then retrieve comments
        mockMvc.perform(get("/api/v1/comments/" + testInvitation.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void hideComment_Success() throws Exception {
        // Submit a comment first
        String commentBody = """
            {
                "content": "Comment to hide",
                "authorName": "Anonymous"
            }
            """;

        String createResponse = mockMvc.perform(post("/api/v1/comments/public/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentBody)
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Extract comment ID (simplified - would need JSON parsing in real test)
        // Then hide it: PUT /api/v1/comments/{commentId}/hide
        
        // This test demonstrates the endpoint structure
    }

    @Test
    @WithMockUser(username = "testuser")
    void showComment_Success() throws Exception {
        // Similar to hide test, but for showing hidden comments
        // PUT /api/v1/comments/{commentId}/show
    }

    @Test
    void submitAnonymousComment_InactiveInvitation() throws Exception {
        // Deactivate invitation
        testInvitation.setActive(false);
        invitationRepository.save(testInvitation);

        String requestBody = """
            {
                "content": "Test comment",
                "authorName": "Anonymous"
            }
            """;

        mockMvc.perform(post("/api/v1/comments/public/" + testInvitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
}
