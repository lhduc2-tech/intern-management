package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.AttendanceRepository;
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

import java.time.LocalDate;
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
class AttendanceOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
    }

    @Test
    void testAttendanceCrudFlow() throws Exception {
        String adminToken = registerAndLogin("attendance-admin", "attendance-admin@example.com", "ADMIN");
        UUID internId = createIntern(adminToken, "Le Hai Duc", "ID-ATT-001");

        LocalDate date = LocalDate.of(2026, 4, 6);
        String createRequest = String.format(
                "{\"internId\":\"%s\",\"date\":\"%s\",\"note\":\"on time\"}",
                internId,
                date
        );

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.internId").value(internId.toString()))
                .andExpect(jsonPath("$.note").value("on time"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andReturn();

        UUID attendanceId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(get("/private/api/v1/attendances/{id}", attendanceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attendanceId.toString()))
                .andExpect(jsonPath("$.date").value(date.toString()));

        mockMvc.perform(post("/private/api/v1/attendances/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(String.format("{\"internId\":\"%s\",\"pageIndex\":0,\"pageSize\":10}", internId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(attendanceId.toString()));

        mockMvc.perform(put("/private/api/v1/attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"note\":\"leave\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("leave"))
                .andExpect(jsonPath("$.updatedBy").exists());

        mockMvc.perform(delete("/private/api/v1/attendances/{id}", attendanceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(attendanceRepository.existsById(attendanceId));
    }

    @Test
    void testSearchAttendances_ByDateRange_ReturnsInclusiveMatches() throws Exception {
        String adminToken = registerAndLogin("attendance-range", "attendance-range@example.com", "ADMIN");
        UUID internId = createIntern(adminToken, "Le Hai Duc", "ID-ATT-RANGE");

        createAttendance(adminToken, internId, LocalDate.of(2026, 4, 1));
        createAttendance(adminToken, internId, LocalDate.of(2026, 4, 6));
        createAttendance(adminToken, internId, LocalDate.of(2026, 4, 10));

        mockMvc.perform(post("/private/api/v1/attendances/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"internId\":\"" + internId + "\",\"fromDate\":\"2026-04-02\",\"toDate\":\"2026-04-08\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].date").value("2026-04-06"));
    }

    @Test
    void testSearchAttendances_WithInvalidDateRange_ReturnsBadRequest() throws Exception {
        String adminToken = registerAndLogin("attendance-range-invalid", "attendance-range-invalid@example.com", "ADMIN");

        mockMvc.perform(post("/private/api/v1/attendances/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"fromDate\":\"2026-04-10\",\"toDate\":\"2026-04-02\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("fromDate must be before or equal to toDate"));
    }

    @Test
    void testCreateAttendance_DuplicateInternDate_ReturnsBadRequest() throws Exception {
        String adminToken = registerAndLogin("attendance-dup", "attendance-dup@example.com", "ADMIN");
        UUID internId = createIntern(adminToken, "Le Hai Duc", "ID-ATT-002");

        String request = String.format(
                "{\"internId\":\"%s\",\"date\":\"2026-04-07\"}",
                internId
        );

        mockMvc.perform(post("/private/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(request))
                .andExpect(status().isOk());

        mockMvc.perform(post("/private/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    void testDeleteAttendance_ForInternRole_ReturnsForbidden() throws Exception {
        String adminToken = registerAndLogin("attendance-del-admin", "attendance-del-admin@example.com", "ADMIN");
        String internToken = registerAndLogin("attendance-del-intern", "attendance-del-intern@example.com", "INTERN");
        UUID internId = createIntern(adminToken, "Le Hai Duc", "ID-ATT-003");

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(String.format("{\"internId\":\"%s\",\"date\":\"2026-04-08\"}", internId)))
                .andExpect(status().isOk())
                .andReturn();

        UUID attendanceId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(delete("/private/api/v1/attendances/{id}", attendanceId)
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
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

    private UUID createAttendance(String token, UUID internId, LocalDate date) throws Exception {
        MvcResult result = mockMvc.perform(post("/private/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("{\"internId\":\"%s\",\"date\":\"%s\"}", internId, date)))
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

