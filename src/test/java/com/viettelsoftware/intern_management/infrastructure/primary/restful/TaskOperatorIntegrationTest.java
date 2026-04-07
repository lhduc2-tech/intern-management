package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.TaskRepository;
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
class TaskOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testTaskCrudFlow() throws Exception {
        String adminToken = registerAndLogin("task-admin", "task-admin@example.com", "ADMIN");
        UUID projectId = createProject(adminToken, "Task Project", "Project for task flow");

        String createRequest = String.format(
                "{\"projectId\":\"%s\",\"title\":\"Implement API\",\"description\":\"CRUD endpoints\",\"priority\":\"high\",\"startDate\":\"2026-04-07\",\"dueDate\":\"2026-04-10\"}",
                projectId
        );

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andReturn();

        UUID taskId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(get("/private/api/v1/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Implement API"));

        mockMvc.perform(post("/private/api/v1/tasks/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"projectId\":\"" + projectId + "\",\"status\":\"todo\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(taskId.toString()));

        mockMvc.perform(put("/private/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"status\":\"in_progress\",\"priority\":\"medium\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.updatedBy").exists());

        mockMvc.perform(delete("/private/api/v1/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    void testCreateTask_ForInternRole_ReturnsForbidden() throws Exception {
        String internToken = registerAndLogin("task-intern", "task-intern@example.com", "INTERN");

        mockMvc.perform(post("/private/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"projectId\":\"00000000-0000-0000-0000-000000000001\",\"title\":\"Forbidden\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    private UUID createProject(String token, String name, String description) throws Exception {
        MvcResult result = mockMvc.perform(post("/private/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("{\"name\":\"%s\",\"description\":\"%s\"}", name, description)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
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

