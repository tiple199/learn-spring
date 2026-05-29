package vn.hoidanit.springrestwithai.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.security.PermissionAuthorizationManager;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Seed Permission + Role cho integration tests và tạo mock JWT
 * với đúng roles claim để pass RBAC check.
 */
@Component
public class TestDataFactory {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionAuthorizationManager permissionAuthorizationManager;

    private static final String TEST_ROLE_NAME = "TEST_ROLE";

    public TestDataFactory(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            PermissionAuthorizationManager permissionAuthorizationManager) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.permissionAuthorizationManager = permissionAuthorizationManager;
    }

    /**
     * Seed permissions cho 1 module (ví dụ: CRUD /api/v1/users).
     * Tạo Role gán tất cả permissions đó, reload cache.
     *
     * @param module   tên module (e.g. "USERS")
     * @param apiPath  ant pattern (e.g. "/api/v1/users/**")
     * @param methods  danh sách HTTP method cần seed (e.g. "GET", "POST", "PUT", "DELETE")
     */
    @Transactional
    public void seedPermissions(String module, String apiPath, String... methods) {
        List<Permission> permissions = new ArrayList<>();
        for (String method : methods) {
            Permission perm = new Permission();
            perm.setName(module + "_" + method);
            perm.setApiPath(apiPath);
            perm.setMethod(method);
            perm.setModule(module);
            permissions.add(permissionRepository.save(perm));
        }

        Role role = roleRepository.findByName(TEST_ROLE_NAME).orElseGet(() -> {
            Role r = new Role();
            r.setName(TEST_ROLE_NAME);
            r.setDescription("Role for integration tests");
            return roleRepository.save(r);
        });

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);

        // Reload cache để PermissionAuthorizationManager nhận permissions mới
        permissionAuthorizationManager.loadCache();
    }

    /**
     * Mock JWT có role TEST_ROLE → pass RBAC check cho permissions đã seed.
     */
    public RequestPostProcessor jwtWithPermission() {
        return jwtWithPermission("test@example.com");
    }

    /**
     * Mock JWT có role TEST_ROLE với custom subject (email).
     * Dùng cho test /auth/me cần subject khớp email trong DB.
     */
    public RequestPostProcessor jwtWithPermission(String email) {
        return jwt().jwt(j -> j
                .subject(email)
                .claim("userId", 1L)
                .claim("roles", List.of("ROLE_" + TEST_ROLE_NAME)));
    }

    /**
     * Mock JWT KHÔNG có role → sẽ bị 403 Forbidden.
     */
    public RequestPostProcessor jwtWithoutPermission() {
        return jwt().jwt(j -> j
                .subject("noperm@example.com")
                .claim("userId", 999L)
                .claim("roles", List.of()));
    }

    /**
     * Cleanup tất cả test data. Gọi trong @AfterEach.
     * Phải xóa role trước (vì có FK tới permission qua join table).
     */
    public void cleanup() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        permissionAuthorizationManager.loadCache();
    }
}
