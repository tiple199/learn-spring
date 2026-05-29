package vn.hoidanit.springrestwithai.feature.role;

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
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.util.ArrayList;
import java.util.List;

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
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("ROLES", "/api/v1/roles", "GET", "POST", "PUT", "DELETE");
        testDataFactory.seedPermissions("ROLES", "/api/v1/roles/**", "GET", "DELETE");
    }

    @AfterEach
    void cleanUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        testDataFactory.cleanup();
    }

    // ========== POST /api/v1/roles ==========

    @Test
    @DisplayName("POST /roles - 201: creates role with permissions and returns response body")
    void createRole_success_returns201() throws Exception {
        Permission savedPermission = permissionRepository.save(
                buildPermission("CREATE_USER", "/api/v1/users", "POST", "USER"));

        CreateRoleRequest request = new CreateRoleRequest(
                "ADMIN", "Full system access", List.of(savedPermission.getId()));

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.name", is("ADMIN")))
                .andExpect(jsonPath("$.data.description", is("Full system access")))
                .andExpect(jsonPath("$.data.permissions[0].name", is("CREATE_USER")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /roles - 400: blank name returns validation error")
    void createRole_blankName_returns400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /roles - 409: duplicate name returns conflict")
    void createRole_duplicateName_returns409() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of());

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    @Test
    @DisplayName("POST /roles - 401: no token returns unauthorized")
    void createRole_noToken_returns401() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of());

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /roles - 403: no permission returns forbidden")
    void createRole_noPermission_returns403() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of());

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithoutPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /roles - 404: missing permissionId returns not found")
    void createRole_withMissingPermissionId_returns404() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of(99999L));

        mockMvc.perform(post("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    // ========== GET /api/v1/roles/{id} ==========

    @Test
    @DisplayName("GET /roles/{id} - 200: returns role")
    void getById_found_returns200() throws Exception {
        Role saved = roleRepository.save(buildRole("ADMIN", "Full access"));

        mockMvc.perform(get("/api/v1/roles/{id}", saved.getId())
                .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("ADMIN")))
                .andExpect(jsonPath("$.data.permissions", notNullValue()));
    }

    @Test
    @DisplayName("GET /roles/{id} - 404: not found returns 404")
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/roles/{id}", 99999L)
                .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /roles/{id} - 401: no token returns unauthorized")
    void getById_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/roles/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /roles/{id} - 403: no permission returns forbidden")
    void getById_noPermission_returns403() throws Exception {
        Role saved = roleRepository.save(buildRole("ADMIN", "Full access"));

        mockMvc.perform(get("/api/v1/roles/{id}", saved.getId())
                .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/roles ==========

    @Test
    @DisplayName("GET /roles - 200: returns paginated list")
    void getAll_returns200WithPaginationMetadata() throws Exception {
        roleRepository.save(buildRole("ADMIN", "Full access"));
        roleRepository.save(buildRole("VIEWER", "Read only"));

        mockMvc.perform(get("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.meta.total").isNumber())
                .andExpect(jsonPath("$.data.result", notNullValue()));
    }

    @Test
    @DisplayName("GET /roles - 403: no permission returns forbidden")
    void getAll_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/roles")
                .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== PUT /api/v1/roles ==========

    @Test
    @DisplayName("PUT /roles - 200: updates and returns updated role")
    void updateRole_success_returns200() throws Exception {
        Role saved = roleRepository.save(buildRole("OLD_NAME", "old desc"));

        UpdateRoleRequest request = new UpdateRoleRequest(
                saved.getId(), "NEW_NAME", "new desc", List.of());

        mockMvc.perform(put("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.name", is("NEW_NAME")))
                .andExpect(jsonPath("$.data.description", is("new desc")));
    }

    @Test
    @DisplayName("PUT /roles - 400: null id returns validation error")
    void updateRole_nullId_returns400() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(null, "NAME", "desc", List.of());

        mockMvc.perform(put("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("PUT /roles - 404: non-existent id returns 404")
    void updateRole_notFound_returns404() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(99999L, "NAME", "desc", List.of());

        mockMvc.perform(put("/api/v1/roles")
                .with(testDataFactory.jwtWithPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("PUT /roles - 403: no permission returns forbidden")
    void updateRole_noPermission_returns403() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(99999L, "NAME", "desc", List.of());

        mockMvc.perform(put("/api/v1/roles")
                .with(testDataFactory.jwtWithoutPermission())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/v1/roles/{id} ==========

    @Test
    @DisplayName("DELETE /roles/{id} - 200: deletes role successfully")
    void deleteRole_success_returns200() throws Exception {
        Role saved = roleRepository.save(buildRole("ADMIN", "Full access"));

        mockMvc.perform(delete("/api/v1/roles/{id}", saved.getId())
                .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("DELETE /roles/{id} - 404: not found returns 404")
    void deleteRole_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/{id}", 99999L)
                .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("DELETE /roles/{id} - 403: no permission returns forbidden")
    void deleteRole_noPermission_returns403() throws Exception {
        Role saved = roleRepository.save(buildRole("ADMIN", "Full access"));

        mockMvc.perform(delete("/api/v1/roles/{id}", saved.getId())
                .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private Role buildRole(String name, String description) {
        Role r = new Role();
        r.setName(name);
        r.setDescription(description);
        r.setPermissions(new ArrayList<>());
        return r;
    }

    private Permission buildPermission(String name, String apiPath, String method, String module) {
        Permission p = new Permission();
        p.setName(name);
        p.setApiPath(apiPath);
        p.setMethod(method);
        p.setModule(module);
        return p;
    }
}
