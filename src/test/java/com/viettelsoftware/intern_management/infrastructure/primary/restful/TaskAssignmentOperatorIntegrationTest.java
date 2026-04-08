package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.TaskAssignmentRepository;
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
class TaskAssignmentOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskAssignmentRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void testTaskAssignmentCrudFlow() throws Exception {
        String adminToken = registerAndLogin("task-assignment-admin", "task-assignment-admin@example.com", "ADMIN");
        UUID projectId = createProject(adminToken, "Assignment Project", "Project for assignment flow");
        UUID taskId = createTask(adminToken, projectId, "Assignment task");
        UUID internId = createIntern(adminToken, "Nguyen Van A", "TA-ID-001");

        String createRequest = String.format(
                "{\"taskId\":\"%s\",\"internId\":\"%s\",\"assignedAt\":\"2026-04-08T09:00:00Z\"}",
                taskId,
                internId
        );

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/task-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.internId").value(internId.toString()))
                .andExpect(jsonPath("$.createdBy").exists())
                .andReturn();

        UUID taskAssignmentId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(get("/private/api/v1/task-assignments/{id}", taskAssignmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskAssignmentId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.internId").value(internId.toString()));

        mockMvc.perform(post("/private/api/v1/task-assignments/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"internId\":\"" + internId + "\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(taskAssignmentId.toString()));

        mockMvc.perform(put("/private/api/v1/task-assignments/{id}", taskAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"assignedAt\":\"2026-04-08T10:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedAt").exists())
                .andExpect(jsonPath("$.updatedBy").exists());

        mockMvc.perform(delete("/private/api/v1/task-assignments/{id}", taskAssignmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(taskAssignmentRepository.existsById(taskAssignmentId));
    }

    @Test
    void testCreateDuplicateTaskAssignment_ReturnsBadRequest() throws Exception {
        String adminToken = registerAndLogin("task-assignment-dup", "task-assignment-dup@example.com", "ADMIN");
        UUID projectId = createProject(adminToken, "Assignment Duplicate Project", "Project for duplicate test");
        UUID taskId = createTask(adminToken, projectId, "Duplicate task");
        UUID internId = createIntern(adminToken, "Nguyen Van B", "TA-ID-002");

        String request = String.format("{\"taskId\":\"%s\",\"internId\":\"%s\"}", taskId, internId);

        mockMvc.perform(post("/private/api/v1/task-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.assignedAt").exists());

        mockMvc.perform(post("/private/api/v1/task-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    void testCreateTaskAssignment_ForInternRole_ReturnsForbidden() throws Exception {
        String adminToken = registerAndLogin("task-assignment-admin-role", "task-assignment-admin-role@example.com", "ADMIN");
        String internToken = registerAndLogin("task-assignment-intern", "task-assignment-intern@example.com", "INTERN");
        UUID projectId = createProject(adminToken, "Assignment Permission Project", "Project for permission test");
        UUID taskId = createTask(adminToken, projectId, "Permission task");
        UUID internId = createIntern(adminToken, "Nguyen Van C", "TA-ID-003");

        mockMvc.perform(post("/private/api/v1/task-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content(String.format("{\"taskId\":\"%s\",\"internId\":\"%s\"}", taskId, internId)))
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

    private UUID createTask(String token, UUID projectId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/private/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("{\"projectId\":\"%s\",\"title\":\"%s\"}", projectId, title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    private UUID createIntern(String token, String fullName, String identityNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("{\"fullName\":\"%s\",\"identityNumber\":\"%s\"}", fullName, identityNumber)))
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

