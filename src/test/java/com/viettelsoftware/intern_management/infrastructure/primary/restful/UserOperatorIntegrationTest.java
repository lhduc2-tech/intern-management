package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordPort passwordPort;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testAdminCrudFlow() throws Exception {
        String adminToken = registerAndLogin("user-crud-admin", "user-crud-admin@example.com", "ADMIN");
        registerUser("managed-user", "managed-user@example.com", "Managed User", "HR");

        UUID userId = userRepository.findByUsername("managed-user").orElseThrow().getId();

        mockMvc.perform(post("/private/api/v1/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"managed-user\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].username").value("managed-user"))
                .andExpect(jsonPath("$.content[0].createdBy").value("system"))
                .andExpect(jsonPath("$.content[0].updatedBy").value("system"));

        mockMvc.perform(get("/private/api/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("managed-user"))
                .andExpect(jsonPath("$.createdBy").value("system"))
                .andExpect(jsonPath("$.updatedBy").value("system"));

        MvcResult updateResult = mockMvc.perform(put("/private/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{" +
                                "\"fullName\":\"Managed User Updated\"," +
                                "\"status\":\"inactive\"," +
                                "\"password\":\"newpassword\"," +
                                "\"roleCodes\":[\"MANAGER\"]" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Managed User Updated"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.roleCodes[0]").value("MANAGER"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andExpect(jsonPath("$.updatedBy").exists())
                .andReturn();

        JsonNode updatedResponse = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(updatedUser.getCreatedBy(), updatedResponse.get("createdBy").asText());
        assertEquals(updatedUser.getUpdatedBy(), updatedResponse.get("updatedBy").asText());
        assertTrue(passwordPort.matches("newpassword", updatedUser.getPasswordHash()));

        mockMvc.perform(delete("/private/api/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void testSearchUsers_ForNonAdminRole_ReturnsForbidden() throws Exception {
        String hrToken = registerAndLogin("user-crud-hr", "user-crud-hr@example.com", "HR");

        mockMvc.perform(post("/private/api/v1/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + hrToken)
                        .content("{\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testCreateUserEndpointRemoved_ReturnsServerErrorForUnsupportedMethod() throws Exception {
        String adminToken = registerAndLogin("user-crud-dup", "user-crud-dup@example.com", "ADMIN");

        mockMvc.perform(post("/private/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{" +
                                "\"username\":\"duplicate-user\"," +
                                "\"email\":\"duplicate-user@example.com\"," +
                                "\"fullName\":\"Duplicate User\"," +
                                "\"password\":\"mypassword\"," +
                                "\"roleCodes\":[\"INTERN\"]" +
                                "}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

    private void registerUser(String username, String email, String fullName, String role) throws Exception {
        String registerRequest = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"password\":\"mypassword\"}",
                username,
                email,
                fullName,
                role
        );

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());
    }

    private String registerAndLogin(String username, String email, String role) throws Exception {
        registerUser(username, email, username, role);

        String loginRequest = String.format(
                "{\"usernameOrEmail\":\"%s\",\"password\":\"mypassword\"}",
                username
        );

        MvcResult result = mockMvc.perform(post("/public/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = jsonNode.get("accessToken").asText();
        assertNotNull(token);
        return token;
    }
}

