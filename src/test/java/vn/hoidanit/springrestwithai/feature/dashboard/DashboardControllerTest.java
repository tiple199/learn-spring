package vn.hoidanit.springrestwithai.feature.dashboard;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.feature.company.Company;
import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void cleanUp() {
        cleanDatabase();
    }

    // ========== GET /api/v1/dashboard ==========

    @Test
    @DisplayName("GET /dashboard - 200: returns dashboard totals")
    void getDashboard_success_returns200WithTotals() throws Exception {
        userRepository.save(buildUser("user1@example.com"));
        userRepository.save(buildUser("user2@example.com"));
        companyRepository.save(buildCompany("HoiDanIT"));
        roleRepository.save(buildRole("ADMIN"));
        roleRepository.save(buildRole("USER"));
        permissionRepository.save(buildPermission("DASHBOARD_GET", "/api/v1/dashboard", "GET", "DASHBOARD"));
        permissionRepository.save(buildPermission("USERS_GET", "/api/v1/users", "GET", "USERS"));
        permissionRepository.save(buildPermission("COMPANIES_GET", "/api/v1/companies", "GET", "COMPANIES"));

        mockMvc.perform(get("/api/v1/dashboard")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.data.totalUsers", is(2)))
                .andExpect(jsonPath("$.data.totalCompanies", is(1)))
                .andExpect(jsonPath("$.data.totalRoles", is(2)))
                .andExpect(jsonPath("$.data.totalPermissions", is(3)));
    }

    @Test
    @DisplayName("GET /dashboard - 200: empty database returns zero totals")
    void getDashboard_emptyDatabase_returns200WithZeroTotals() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.totalUsers", is(0)))
                .andExpect(jsonPath("$.data.totalCompanies", is(0)))
                .andExpect(jsonPath("$.data.totalRoles", is(0)))
                .andExpect(jsonPath("$.data.totalPermissions", is(0)));
    }

    @Test
    @DisplayName("GET /dashboard - 401: no token")
    void getDashboard_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)));
    }

    // ========== helpers ==========

    private void cleanDatabase() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        companyRepository.deleteAll();
    }

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        user.setPassword("password");
        return user;
    }

    private Company buildCompany(String name) {
        Company company = new Company();
        company.setName(name);
        return company;
    }

    private Role buildRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("Test role");
        return role;
    }

    private Permission buildPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permission;
    }
}
