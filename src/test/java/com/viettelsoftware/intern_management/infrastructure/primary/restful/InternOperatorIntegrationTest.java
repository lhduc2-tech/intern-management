package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.intern_management.domain.entity.UserEntity;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class InternOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternRepository internRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        internRepository.deleteAll();
    }

    @Test
    void testCrudFlow() throws Exception {
        String adminToken = registerAndLogin("intern-crud-admin", "intern-crud-admin@example.com", "ADMIN");
        String internToken = registerAndLogin("intern-crud-intern", "intern-crud-intern@example.com", "INTERN");
        UUID userId = UUID.randomUUID();

        String createRequest = String.format(
                "{\"fullName\":\"Le Hai Duc\",\"userId\":\"%s\",\"companyAccount\":\"acct-001\",\"identityNumber\":\"ID-001\",\"technology\":\"Java\",\"mentorId\":\"%s\",\"organization\":\"Viettel\",\"gender\":\"MALE\",\"dateOfBirth\":\"2000-01-01\",\"level\":\"FRESHER\",\"major\":\"IT\",\"university\":\"HUST\",\"phone\":\"0123456789\",\"permanentAddress\":\"Hanoi\",\"position\":\"Intern Developer\",\"internshipStartDate\":\"2026-01-01\",\"internshipEndDate\":\"2026-06-01\",\"acceptanceDocument\":\"offer.pdf\"}",
                userId,
                UUID.randomUUID()
        );

        MvcResult createResult = mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.graduated").value(false))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID internId = UUID.fromString(created.get("id").asText());
        UUID createdUserId = UUID.fromString(created.get("userId").asText());

        UserEntity createdUser = userRepository.findById(createdUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("duclh", createdUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("duclh@viettelsoftware.com", createdUser.getEmail());
        org.junit.jupiter.api.Assertions.assertTrue(createdUser.getRoles().stream().anyMatch(role -> "INTERN".equals(role.getCode())));
        org.junit.jupiter.api.Assertions.assertEquals(createdUser.getEmail(), created.get("companyAccount").asText());

        mockMvc.perform(get("/private/api/v1/interns/{id}", internId)
                        .header("Authorization", "Bearer " + internToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(internId.toString()))
                .andExpect(jsonPath("$.identityNumber").value("ID-001"));

        String updateRequest = String.format(
                "{\"fullName\":\"Le Hai Duc\",\"userId\":\"%s\",\"status\":\"ONBOARDING\",\"companyAccount\":\"%s\",\"identityNumber\":\"ID-001\",\"technology\":\"Spring Boot\",\"mentorId\":\"%s\",\"organization\":\"Viettel\",\"gender\":\"MALE\",\"dateOfBirth\":\"2000-01-01\",\"level\":\"JUNIOR\",\"major\":\"IT\",\"university\":\"HUST\",\"phone\":\"0123456789\",\"permanentAddress\":\"Hanoi\",\"position\":\"Intern Developer\",\"internshipStartDate\":\"2026-01-01\",\"internshipEndDate\":\"2026-06-01\",\"acceptanceDocument\":\"offer-v2.pdf\"}",
                userId,
                createdUser.getEmail(),
                UUID.randomUUID()
        );

        MvcResult updateResult = mockMvc.perform(put("/private/api/v1/interns/{id}", internId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONBOARDING"))
                .andExpect(jsonPath("$.technology").value("Spring Boot"))
                .andExpect(jsonPath("$.acceptanceDocument").value("offer-v2.pdf"))
                .andExpect(jsonPath("$.createdBy").exists())
                .andExpect(jsonPath("$.updatedBy").exists())
                .andReturn();

        JsonNode updatedResponse = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        com.viettelsoftware.intern_management.domain.entity.InternEntity updatedIntern = internRepository.findById(internId).orElseThrow();
        assertEquals(updatedIntern.getCreatedBy(), updatedResponse.get("createdBy").asText());
        assertEquals(updatedIntern.getUpdatedBy(), updatedResponse.get("updatedBy").asText());

        mockMvc.perform(delete("/private/api/v1/interns/{id}", internId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertFalse(internRepository.existsById(internId));
    }

    @Test
    void testSearchInterns_ReturnsPagedAndFilteredResults() throws Exception {
        String adminToken = registerAndLogin("intern-search", "intern-search@example.com", "ADMIN");
        UUID mentorId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        UUID firstUserId = createIntern(adminToken, UUID.randomUUID(), "acct-001", "ID-201", projectId, mentorId, "Viettel");
        UUID secondUserId = createIntern(adminToken, UUID.randomUUID(), "acct-002", "ID-202", projectId, mentorId, "Viettel");
        String firstEmail = userRepository.findById(firstUserId).orElseThrow().getEmail();
        String secondEmail = userRepository.findById(secondUserId).orElseThrow().getEmail();

        String searchBodyPage1 = String.format(
                "{\"organization\":\"Viettel\",\"status\":\"ACTIVE\",\"graduated\":false,\"projectId\":\"%s\",\"mentorId\":\"%s\",\"pageIndex\":0,\"pageSize\":1,\"sort\":\"companyAccount\",\"direction\":\"ASC\"}",
                projectId,
                mentorId
        );

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(searchBodyPage1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].companyAccount").value(secondEmail));

        String searchBodyPage2 = String.format(
                "{\"organization\":\"Viettel\",\"status\":\"ACTIVE\",\"graduated\":false,\"projectId\":\"%s\",\"mentorId\":\"%s\",\"pageIndex\":1,\"pageSize\":1,\"sort\":\"companyAccount\",\"direction\":\"ASC\"}",
                projectId,
                mentorId
        );

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(searchBodyPage2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyAccount").value(firstEmail));
    }

    @Test
    void testSearchInterns_WithoutBody_ReturnsAllInternsWithDefaultPagination() throws Exception {
        String adminToken = registerAndLogin("intern-search-empty", "intern-search-empty@example.com", "ADMIN");

        createIntern(adminToken, UUID.randomUUID(), "acct-empty-1", "ID-E-1", UUID.randomUUID(), UUID.randomUUID(), "Viettel");
        createIntern(adminToken, UUID.randomUUID(), "acct-empty-2", "ID-E-2", UUID.randomUUID(), UUID.randomUUID(), "Viettel");

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void testCreateIntern_AutoGeneratesUniqueUsername() throws Exception {
        String adminToken = registerAndLogin("intern-username", "intern-username@example.com", "ADMIN");

        MvcResult first = mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"fullName\":\"Lê Hải Đức\",\"companyAccount\":\"acct-u-1\",\"identityNumber\":\"ID-U-1\"}"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"fullName\":\"Lê Hải Đức\",\"companyAccount\":\"acct-u-2\",\"identityNumber\":\"ID-U-2\"}"))
                .andExpect(status().isOk())
                .andReturn();

        UUID firstUserId = UUID.fromString(objectMapper.readTree(first.getResponse().getContentAsString()).get("userId").asText());
        UUID secondUserId = UUID.fromString(objectMapper.readTree(second.getResponse().getContentAsString()).get("userId").asText());

        UserEntity firstUser = userRepository.findById(firstUserId).orElseThrow();
        UserEntity secondUser = userRepository.findById(secondUserId).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals("duclh", firstUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("duclh1", secondUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("duclh@viettelsoftware.com", firstUser.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("duclh1@viettelsoftware.com", secondUser.getEmail());
    }

    @Test
    void testSearchInterns_WithAllFilters_ReturnsSingleMatch() throws Exception {
        String hrToken = registerAndLogin("intern-search-all", "intern-search-all@example.com", "HR");
        UUID userId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        UUID generatedUserId = createIntern(hrToken, userId, "acct-900", "ID-900", projectId, mentorId, "FPT");
        String generatedEmail = userRepository.findById(generatedUserId).orElseThrow().getEmail();

        String searchBody = String.format(
                "{\"userId\":\"%s\",\"status\":\"ACTIVE\",\"projectId\":\"%s\",\"graduated\":false,\"companyAccount\":\"%s\",\"mentorId\":\"%s\",\"organization\":\"FPT\",\"pageIndex\":0,\"pageSize\":10}",
                generatedUserId,
                projectId,
                generatedEmail,
                mentorId
        );

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + hrToken)
                        .content(searchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(generatedUserId.toString()))
                .andExpect(jsonPath("$.content[0].companyAccount").value(generatedEmail))
                .andExpect(jsonPath("$.content[0].organization").value("FPT"));
    }

    @Test
    void testSearchInterns_ByPartialOrganization_WithVietnameseText_ReturnsMatch() throws Exception {
        String adminToken = registerAndLogin("intern-search-org", "intern-search-org@example.com", "ADMIN");

        createIntern(
                adminToken,
                UUID.randomUUID(),
                "acct-org-1",
                "ID-ORG-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Trung tâm phần mềm viễn thông"
        );

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"organization\":\"trung tâm\",\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].organization").value("Trung tâm phần mềm viễn thông"));
    }

    @Test
    void testSearchInterns_ForInternRole_ReturnsForbidden() throws Exception {
        String internToken = registerAndLogin("intern-search-forbidden", "intern-search-forbidden@example.com", "INTERN");

        mockMvc.perform(post("/private/api/v1/interns/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"pageIndex\":0,\"pageSize\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testCreateIntern_ForInternRole_ReturnsForbidden() throws Exception {
        String internToken = registerAndLogin("intern-create-forbidden", "intern-create-forbidden@example.com", "INTERN");

        mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"companyAccount\":\"acct-x\",\"identityNumber\":\"ID-x\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testCreateDuplicateCompanyAccountReturnsBadRequest() throws Exception {
        String adminToken = registerAndLogin("intern-dup", "intern-dup@example.com", "ADMIN");
        UUID userId1 = UUID.randomUUID();

        UUID createdUserId = createIntern(adminToken, userId1, "acct-dup", "ID-100", UUID.randomUUID(), UUID.randomUUID(), "Viettel");

        UserEntity savedUser = userRepository.findById(createdUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(savedUser.getEmail(), internRepository.findAll().get(0).getCompanyAccount());
        org.junit.jupiter.api.Assertions.assertNotEquals("acct-dup", savedUser.getEmail());
    }

    @Test
    void testCreateWithInvalidDateReturnsValidationError() throws Exception {
        String adminToken = registerAndLogin("intern-invalid", "intern-invalid@example.com", "ADMIN");
        UUID userId = UUID.randomUUID();
        String request = String.format(
                "{\"fullName\":\"Nguyen Van A\",\"userId\":\"%s\",\"dateOfBirth\":\"2999-01-01\"}",
                userId
        );

        mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.dateOfBirth").value("Date of birth must be in the past"));
    }

    private UUID createIntern(
            String token,
            UUID userId,
            String companyAccount,
            String identityNumber,
            UUID projectId,
            UUID mentorId,
            String organization
    ) throws Exception {
        String request = String.format(
                "{\"fullName\":\"Le Hai Duc\",\"userId\":\"%s\",\"companyAccount\":\"%s\",\"identityNumber\":\"%s\",\"projectId\":\"%s\",\"mentorId\":\"%s\",\"organization\":\"%s\"}",
                userId,
                companyAccount,
                identityNumber,
                projectId,
                mentorId,
                organization
        );

        MvcResult result = mockMvc.perform(post("/private/api/v1/interns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("userId").asText());
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



