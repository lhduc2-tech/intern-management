package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.domain.entity.ProjectEntity;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.ProjectRepository;
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
class ProjectOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void testProjectCrudFlow() throws Exception {
        String adminToken = registerAndLogin("project-admin", "project-admin@example.com", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"Project Alpha\",\"description\":\"Alpha description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Project Alpha"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID projectId = UUID.fromString(createdJson.get("id").asText());

        ProjectEntity persistedAfterCreate = projectRepository.findById(projectId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(persistedAfterCreate.getCreatedBy(), createdJson.get("createdBy").asText());
        org.junit.jupiter.api.Assertions.assertEquals(persistedAfterCreate.getUpdatedBy(), createdJson.get("updatedBy").asText());

        mockMvc.perform(get("/private/api/v1/projects/{id}", projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Project Alpha"));

        mockMvc.perform(post("/private/api/v1/projects/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(projectId.toString()));

        mockMvc.perform(post("/private/api/v1/projects/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"alpha\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Project Alpha"));

        MvcResult updateResult = mockMvc.perform(put("/private/api/v1/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"name\":\"Project Alpha Updated\",\"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project Alpha Updated"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andExpect(jsonPath("$.updatedBy").exists())
                .andReturn();

        JsonNode updatedJson = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        ProjectEntity persistedAfterUpdate = projectRepository.findById(projectId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(persistedAfterUpdate.getCreatedBy(), updatedJson.get("createdBy").asText());
        org.junit.jupiter.api.Assertions.assertEquals(persistedAfterUpdate.getUpdatedBy(), updatedJson.get("updatedBy").asText());

        mockMvc.perform(delete("/private/api/v1/projects/{id}", projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(projectRepository.existsById(projectId));
    }

    @Test
    void testProjectCreate_ForNonAdminRole_ReturnsForbidden() throws Exception {
        String internToken = registerAndLogin("project-intern", "project-intern@example.com", "INTERN");

        mockMvc.perform(post("/private/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"name\":\"Project Forbidden\"}"))
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

