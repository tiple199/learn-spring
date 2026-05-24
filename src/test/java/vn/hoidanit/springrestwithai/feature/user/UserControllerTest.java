package vn.hoidanit.springrestwithai.feature.user;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.feature.company.Company;
import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        companyRepository.deleteAll();
    }

    // ========== POST /api/v1/users ==========

    @Test
    @DisplayName("POST /users: valid request without company/roles → 201")
    void createUser_validRequest_returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A", "a@example.com", "password123",
                25, "Hanoi", User.GenderEnum.MALE, null, null);

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.email", is("a@example.com")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /users: valid request with company and roles → 201 with associations")
    void createUser_withCompanyAndRoles_returns201WithAssociations() throws Exception {
        Company company = saveCompany("Tech Corp");
        Role role = saveRole("ADMIN");

        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van B", "b@example.com", "password123",
                30, "HCM", User.GenderEnum.FEMALE,
                company.getId(), List.of(role.getId()));

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.company.id", is(company.getId().intValue())))
                .andExpect(jsonPath("$.data.roles[0].id", is(role.getId().intValue())));
    }

    @Test
    @DisplayName("POST /users: missing name → 400")
    void createUser_missingName_returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "", "c@example.com", "password123",
                25, null, null, null, null);

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /users: duplicate email → 409")
    void createUser_duplicateEmail_returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A", "dup@example.com", "password123",
                25, null, null, null, null);

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /users: no auth → 401")
    void createUser_noAuth_returns401() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A", "a@example.com", "password123",
                25, null, null, null, null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /api/v1/users/{id} ==========

    @Test
    @DisplayName("GET /users/{id}: existing id → 200")
    void getUserById_existingId_returns200() throws Exception {
        User saved = saveUser("Nguyen Van A", "a@test.com");

        mockMvc.perform(get("/api/v1/users/" + saved.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.email", is("a@test.com")));
    }

    @Test
    @DisplayName("GET /users/{id}: not found → 404")
    void getUserById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/9999")
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /users/{id}: no auth → 401")
    void getUserById_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /api/v1/users ==========

    @Test
    @DisplayName("GET /users: returns 200 with paged structure")
    void getAllUsers_returns200WithPage() throws Exception {
        saveUser("User A", "a@test.com");
        saveUser("User B", "b@test.com");

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.content", notNullValue()))
                .andExpect(jsonPath("$.data.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /users: no auth → 401")
    void getAllUsers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    // ========== PUT /api/v1/users ==========

    @Test
    @DisplayName("PUT /users: valid request → 200")
    void updateUser_validRequest_returns200() throws Exception {
        User saved = saveUser("Old Name", "old@test.com");

        UpdateUserRequest request = new UpdateUserRequest(
                saved.getId(), "New Name", "new@test.com",
                30, "HCM", User.GenderEnum.FEMALE, null, null);

        mockMvc.perform(put("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.email", is("new@test.com")));
    }

    @Test
    @DisplayName("PUT /users: user not found → 404")
    void updateUser_notFound_returns404() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                9999L, "Name", "e@test.com", null, null, null, null, null);

        mockMvc.perform(put("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /users: missing email → 400")
    void updateUser_missingEmail_returns400() throws Exception {
        User saved = saveUser("Name", "valid@test.com");

        UpdateUserRequest request = new UpdateUserRequest(
                saved.getId(), "Name", "", null, null, null, null, null);

        mockMvc.perform(put("/api/v1/users")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    // ========== DELETE /api/v1/users/{id} ==========

    @Test
    @DisplayName("DELETE /users/{id}: existing id → 200")
    void deleteUser_existingId_returns200() throws Exception {
        User saved = saveUser("To Delete", "delete@test.com");

        mockMvc.perform(delete("/api/v1/users/" + saved.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /users/{id}: not found → 404")
    void deleteUser_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/users/9999")
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /users/{id}: no auth → 401")
    void deleteUser_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== HELPERS ==========

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("$2a$10$encodedpassword");
        return userRepository.save(user);
    }

    private Company saveCompany(String name) {
        Company company = new Company();
        company.setName(name);
        return companyRepository.save(company);
    }

    private Role saveRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }
}
