package com.viettelsoftware.intern_management.infrastructure.primary.restful;


import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordPort passwordPort;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean the database before each test
    }

    // --- REGISTER TESTS ---

    @Test
    void testRegister_Success() throws Exception {
        String requestParams = "{\"username\":\"newuser\", \"email\":\"newuser@example.com\", \"fullName\":\"New User\", \"password\":\"mypassword\"}";

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isOk());

        assertTrue(userRepository.existsByUsername("newuser"));
        assertEquals("New User", userRepository.findByUsername("newuser").orElseThrow().getFullName());
    }

    @Test
    void testAuditFields_DefaultToSystem_WhenNoAuthentication() {
        UserEntity user = UserEntity.builder()
                .username("audited-user")
                .email("audited@example.com")
                .fullName("Audited User")
                .passwordHash("hashed-pass")
                .status("ACTIVE")
                .build();

        UserEntity saved = userRepository.saveAndFlush(user);

        assertEquals("system", saved.getCreatedBy());
        assertEquals("system", saved.getUpdatedBy());
    }

    @Test
    void testRegister_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // Setup existing user
        UserEntity existingUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("existinguser")
                .email("existing@example.com")
                .passwordHash("hashedpass")
                .build();
        userRepository.save(existingUser);

        String requestParams = "{\"username\":\"existinguser\", \"email\":\"new@example.com\", \"fullName\":\"Existing User\", \"password\":\"mypassword\"}";

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Username already exists"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void testRegister_ValidationFails_ReturnsBadRequest() throws Exception {
        // Invalid email format, blank fullName and too short password
        String requestParams = "{\"username\":\"validuser\", \"email\":\"invalid-email\", \"fullName\":\"\", \"password\":\"123\"}";

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.email").value("Email should be valid"))
                .andExpect(jsonPath("$.details.fullName").value("Full name must not be blank"))
                .andExpect(jsonPath("$.details.password").value("Password must be between 6 and 256 characters"));
    }

    @Test
    void testDataIntegrityException_ReturnsUnifiedErrorEnvelope() throws Exception {
        mockMvc.perform(get("/test-errors/data-integrity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DATA_INTEGRITY_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Username already exists."))
                .andExpect(jsonPath("$.details.rootCause").value("users_username_key"));
    }

    @Test
    void testGenericException_ReturnsUnifiedErrorEnvelope() throws Exception {
        mockMvc.perform(get("/test-errors/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("boom"))
                .andExpect(jsonPath("$.details.exception").value("IllegalStateException"));
    }

    // --- LOGIN TESTS ---

    @Test
    void testLogin_Success() throws Exception {
        // Setup existing user
        UserEntity existingUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("loginuser")
                .email("login@example.com")
                .passwordHash(passwordPort.encode("correctpassword"))
                .build();
        userRepository.save(existingUser);

        String requestParams = "{\"usernameOrEmail\":\"loginuser\", \"password\":\"correctpassword\"}";

        mockMvc.perform(post("/public/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString());

        assertNotNull(userRepository.findByUsername("loginuser").orElseThrow().getLastLoginAt());
    }

    @Test
    void testLogin_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Setup existing user
        UserEntity existingUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("loginuser")
                .email("login@example.com")
                .passwordHash(passwordPort.encode("correctpassword"))
                .build();
        userRepository.save(existingUser);

        // Wrong password
        String requestParams = "{\"usernameOrEmail\":\"loginuser\", \"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/public/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void testLogin_ValidationFails_ReturnsBadRequest() throws Exception {
        // Missing usernameOrEmail
        String requestParams = "{\"usernameOrEmail\":\"\", \"password\":\"mypassword\"}";

        mockMvc.perform(post("/public/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestParams))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.usernameOrEmail").value("Username or email must not be blank"));
    }
}
