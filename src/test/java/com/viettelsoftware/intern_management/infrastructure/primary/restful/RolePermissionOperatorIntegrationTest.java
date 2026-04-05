package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RolePermissionOperatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testAdminCanCreatePermissionAndAssignToRole() throws Exception {
        registerUser("admin01", "admin01@example.com", "Admin User", "ADMIN");
        registerUser("hr01", "hr01@example.com", "HR User", "HR");
        String adminToken = loginAndGetAccessToken("admin01");

        createPermission(adminToken, "INTERN_READ", "Read intern data");
        createPermission(adminToken, "INTERVIEW_WRITE", "Write interview results");

        mockMvc.perform(put("/private/api/v1/rbac/roles/HR/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"permissionCodes\":[\"INTERN_READ\",\"INTERVIEW_WRITE\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("HR"))
                .andExpect(jsonPath("$.permissionCodes").isArray());
    }

    @Test
    void testAdminCanCreateRole() throws Exception {
        registerUser("admin-role", "admin-role@example.com", "Admin User", "ADMIN");
        String adminToken = loginAndGetAccessToken("admin-role");

        mockMvc.perform(post("/private/api/v1/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"code\":\"MANAGER\",\"name\":\"Manager\",\"description\":\"Manages intern operations\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MANAGER"))
                .andExpect(jsonPath("$.name").value("Manager"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/private/api/v1/rbac/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='MANAGER')]").exists());
    }

    @Test
    void testAdminCannotCreateDuplicateRole() throws Exception {
        registerUser("admin-dup-role", "admin-dup-role@example.com", "Admin User", "ADMIN");
        String adminToken = loginAndGetAccessToken("admin-dup-role");

        mockMvc.perform(post("/private/api/v1/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"code\":\"MANAGER\",\"name\":\"Manager\",\"description\":\"Manages intern operations\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/private/api/v1/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"code\":\"manager\",\"name\":\"Manager 2\",\"description\":\"Duplicate role\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Role already exists: MANAGER"));
    }

    @Test
    void testNonAdminCannotCreateRole() throws Exception {
        registerUser("mentor-role", "mentor-role@example.com", "Mentor User", "MENTOR");
        String mentorToken = loginAndGetAccessToken("mentor-role");

        mockMvc.perform(post("/private/api/v1/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentorToken)
                        .content("{\"code\":\"MANAGER\",\"name\":\"Manager\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testInternCannotManagePermissions() throws Exception {
        registerUser("intern01", "intern01@example.com", "Intern User", "INTERN");
        String internToken = loginAndGetAccessToken("intern01");

        mockMvc.perform(post("/private/api/v1/rbac/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internToken)
                        .content("{\"code\":\"PROJECT_APPROVE\",\"description\":\"Approve projects\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testCreateRole_WithUnsupportedCode_ReturnsBadRequest() throws Exception {
        registerUser("admin-unsupported", "admin-unsupported@example.com", "Admin User", "ADMIN");
        String adminToken = loginAndGetAccessToken("admin-unsupported");

        mockMvc.perform(post("/private/api/v1/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"code\":\"SUPER_USER\",\"name\":\"Super User\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    void testAuthenticatedUserCanReadRolesAndPermissions() throws Exception {
        registerUser("admin01", "admin01@example.com", "Admin User", "ADMIN");
        registerUser("mentor01", "mentor01@example.com", "Mentor User", "MENTOR");
        String adminToken = loginAndGetAccessToken("admin01");
        createPermission(adminToken, "INTERN_READ", "Read intern data");
        String mentorToken = loginAndGetAccessToken("mentor01");

        mockMvc.perform(get("/private/api/v1/rbac/roles")
                        .header("Authorization", "Bearer " + mentorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists());

        mockMvc.perform(get("/private/api/v1/rbac/permissions")
                        .header("Authorization", "Bearer " + mentorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists());
    }

    private void registerUser(String username, String email, String fullName, String role) throws Exception {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"password\":\"mypassword\"}",
                username, email, fullName, role
        );

        mockMvc.perform(post("/public/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    private void createPermission(String adminToken, String code, String description) throws Exception {
        String requestBody = String.format("{\"code\":\"%s\",\"description\":\"%s\"}", code, description);

        mockMvc.perform(post("/private/api/v1/rbac/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code));
    }

    private String loginAndGetAccessToken(String usernameOrEmail) throws Exception {
        String requestBody = String.format("{\"usernameOrEmail\":\"%s\",\"password\":\"mypassword\"}", usernameOrEmail);

        MvcResult result = mockMvc.perform(post("/public/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("accessToken").asText();
    }
}

