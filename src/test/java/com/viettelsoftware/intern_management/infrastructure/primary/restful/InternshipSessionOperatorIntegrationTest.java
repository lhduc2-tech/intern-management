package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternshipSessionRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class InternshipSessionOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternshipSessionRepository internshipSessionRepository;

    @BeforeEach
    void setUp() {
        internshipSessionRepository.deleteAll();
    }

    @Test
    void testInternshipSessionCrudFlow() throws Exception {
        String adminToken = registerAndLogin("session-admin", "session-admin@example.com", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/internship-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"Session 2026\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Session 2026"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(createdJson.get("id").asText());

        mockMvc.perform(get("/private/api/v1/internship-sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.name").value("Session 2026"));

        mockMvc.perform(post("/private/api/v1/internship-sessions/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"session\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(sessionId.toString()));

        mockMvc.perform(put("/private/api/v1/internship-sessions/{id}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"Session 2026 Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Session 2026 Updated"));

        mockMvc.perform(delete("/private/api/v1/internship-sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(internshipSessionRepository.existsById(sessionId));
    }

    @Test
    void testCreateInternshipSession_ForInternRole_ReturnsForbidden() throws Exception {
        String internToken = registerAndLogin("session-intern", "session-intern@example.com", "INTERN");

        mockMvc.perform(post("/private/api/v1/internship-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"name\":\"Forbidden Session\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    private String registerAndLogin(String username, String email, String role) throws Exception {
        String registerRequest = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"password\":\"mypassword\"}",
                username,
                email,
                username,
                role
        );

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

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

