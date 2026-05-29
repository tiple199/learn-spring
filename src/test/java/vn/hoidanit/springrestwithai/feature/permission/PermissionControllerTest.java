package vn.hoidanit.springrestwithai.feature.permission;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("PERMISSIONS", "/api/v1/permissions", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("PERMISSIONS", "/api/v1/permissions/**", "GET", "DELETE");
    }

    @AfterEach
    void cleanUp() {
        // cleanup() xóa roles (+ join table) trước, rồi xóa seeded permissions
        // Sau đó xóa thêm permissions do test tạo (nếu còn)
        testDataFactory.cleanup();
        permissionRepository.deleteAll();
    }

    // ========== POST /api/v1/permissions ==========

    @Test
    @DisplayName("POST /permissions - 201: creates permission and returns response body")
    void createPermission_success_returns201() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        mockMvc.perform(post("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.name", is("CREATE_USER")))
                .andExpect(jsonPath("$.data.apiPath", is("/api/v1/users")))
                .andExpect(jsonPath("$.data.method", is("POST")))
                .andExpect(jsonPath("$.data.module", is("USER")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /permissions - 400: blank fields return validation error")
    void createPermission_blankFields_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /permissions - 409: duplicate apiPath+method returns conflict")
    void createPermission_duplicate_returns409() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        mockMvc.perform(post("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /permissions - 401: no token returns unauthorized")
    void createPermission_noToken_returns401() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /permissions - 403: no permission returns forbidden")
    void createPermission_noPermission_returns403() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        mockMvc.perform(post("/api/v1/permissions")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/permissions/{id} ==========

    @Test
    @DisplayName("GET /permissions/{id} - 200: returns permission")
    void getById_found_returns200() throws Exception {
        Permission saved = permissionRepository.save(buildPermission("VIEW_USERS", "/api/v1/users", "GET", "USER"));

        mockMvc.perform(get("/api/v1/permissions/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("VIEW_USERS")));
    }

    @Test
    @DisplayName("GET /permissions/{id} - 404: not found returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /permissions/{id} - 401: no token returns unauthorized")
    void getById_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/permissions/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /permissions/{id} - 403: no permission returns forbidden")
    void getById_noPermission_returns403() throws Exception {
        Permission saved = permissionRepository.save(buildPermission("VIEW_USERS", "/api/v1/users", "GET", "USER"));

        mockMvc.perform(get("/api/v1/permissions/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/permissions ==========

    @Test
    @DisplayName("GET /permissions - 200: returns paginated list")
    void getAll_returns200WithPaginationMetadata() throws Exception {
        permissionRepository.save(buildPermission("CREATE_USER", "/api/v1/users", "POST", "USER"));
        permissionRepository.save(buildPermission("DELETE_USER", "/api/v1/users/1", "DELETE", "USER"));

        mockMvc.perform(get("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.total").isNumber())
                .andExpect(jsonPath("$.data.result", notNullValue()));
    }

    @Test
    @DisplayName("GET /permissions - 403: no permission returns forbidden")
    void getAll_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/permissions")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== PUT /api/v1/permissions ==========

    @Test
    @DisplayName("PUT /permissions - 200: updates and returns updated permission")
    void updatePermission_success_returns200() throws Exception {
        Permission saved = permissionRepository.save(buildPermission("OLD_NAME", "/api/v1/old", "GET", "USER"));

        UpdatePermissionRequest request = new UpdatePermissionRequest(
                saved.getId(), "NEW_NAME", "/api/v1/new", "PUT", "ROLE");

        mockMvc.perform(put("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.name", is("NEW_NAME")))
                .andExpect(jsonPath("$.data.apiPath", is("/api/v1/new")))
                .andExpect(jsonPath("$.data.method", is("PUT")));
    }

    @Test
    @DisplayName("PUT /permissions - 400: null id returns validation error")
    void updatePermission_nullId_returns400() throws Exception {
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                null, "NAME", "/api/v1/path", "GET", "USER");

        mockMvc.perform(put("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("PUT /permissions - 404: non-existent id returns 404")
    void updatePermission_notFound_returns404() throws Exception {
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                99999L, "NAME", "/api/v1/path", "GET", "USER");

        mockMvc.perform(put("/api/v1/permissions")
                        .with(testDataFactory.jwtWithPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /permissions - 403: no permission returns forbidden")
    void updatePermission_noPermission_returns403() throws Exception {
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                99999L, "NAME", "/api/v1/path", "GET", "USER");

        mockMvc.perform(put("/api/v1/permissions")
                        .with(testDataFactory.jwtWithoutPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/v1/permissions/{id} ==========

    @Test
    @DisplayName("DELETE /permissions/{id} - 200: deletes permission successfully")
    void deletePermission_success_returns200() throws Exception {
        Permission saved = permissionRepository.save(buildPermission("DELETE_USER", "/api/v1/users/1", "DELETE", "USER"));

        mockMvc.perform(delete("/api/v1/permissions/{id}", saved.getId())
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /permissions/{id} - 404: not found returns 404")
    void deletePermission_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/permissions/{id}", 99999L)
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /permissions/{id} - 403: no permission returns forbidden")
    void deletePermission_noPermission_returns403() throws Exception {
        Permission saved = permissionRepository.save(buildPermission("DELETE_USER", "/api/v1/users/1", "DELETE", "USER"));

        mockMvc.perform(delete("/api/v1/permissions/{id}", saved.getId())
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private Permission buildPermission(String name, String apiPath, String method, String module) {
        Permission p = new Permission();
        p.setName(name);
        p.setApiPath(apiPath);
        p.setMethod(method);
        p.setModule(module);
        return p;
    }
}
