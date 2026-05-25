package vn.hoidanit.springrestwithai.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import vn.hoidanit.springrestwithai.feature.company.Company;
import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

/**
 * Seeds initial data on application startup.
 * Only runs when app.seed-data=true (dev/test environments).
 * Skips if data already exists (checks user count).
 */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final PermissionRepository permissionRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            PermissionRepository permissionRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info(">>> Database already seeded — skipping");
            return;
        }

        log.info(">>> Seeding database...");

        List<Permission> permissions = seedPermissions();
        List<Company> companies = seedCompanies();
        List<Role> roles = seedRoles(permissions);
        seedUsers(roles, companies);

        log.info(">>> Database seeded successfully");
    }

    // ═══════════════════════════════════════
    // Step 1: Permissions (21 records)
    // ═══════════════════════════════════════

    private List<Permission> seedPermissions() {
        List<Permission> permissions = List.of(
                // USER module
                createPermission("CREATE_USER", "/api/v1/users", "POST", "USER"),
                createPermission("UPDATE_USER", "/api/v1/users", "PUT", "USER"),
                createPermission("DELETE_USER", "/api/v1/users/{id}", "DELETE", "USER"),
                createPermission("VIEW_USERS", "/api/v1/users", "GET", "USER"),
                createPermission("VIEW_USER", "/api/v1/users/{id}", "GET", "USER"),

                // COMPANY module
                createPermission("CREATE_COMPANY", "/api/v1/companies", "POST", "COMPANY"),
                createPermission("UPDATE_COMPANY", "/api/v1/companies", "PUT", "COMPANY"),
                createPermission("DELETE_COMPANY", "/api/v1/companies/{id}", "DELETE", "COMPANY"),
                createPermission("VIEW_COMPANIES", "/api/v1/companies", "GET", "COMPANY"),
                createPermission("VIEW_COMPANY", "/api/v1/companies/{id}", "GET", "COMPANY"),

                // ROLE module
                createPermission("CREATE_ROLE", "/api/v1/roles", "POST", "ROLE"),
                createPermission("UPDATE_ROLE", "/api/v1/roles", "PUT", "ROLE"),
                createPermission("DELETE_ROLE", "/api/v1/roles/{id}", "DELETE", "ROLE"),
                createPermission("VIEW_ROLES", "/api/v1/roles", "GET", "ROLE"),
                createPermission("VIEW_ROLE", "/api/v1/roles/{id}", "GET", "ROLE"),

                // PERMISSION module
                createPermission("CREATE_PERMISSION", "/api/v1/permissions", "POST", "PERMISSION"),
                createPermission("UPDATE_PERMISSION", "/api/v1/permissions", "PUT", "PERMISSION"),
                createPermission("DELETE_PERMISSION", "/api/v1/permissions/{id}", "DELETE", "PERMISSION"),
                createPermission("VIEW_PERMISSIONS", "/api/v1/permissions", "GET", "PERMISSION"),
                createPermission("VIEW_PERMISSION", "/api/v1/permissions/{id}", "GET", "PERMISSION"),

                // FILE module
                createPermission("UPLOAD_FILE", "/api/v1/files", "POST", "FILE"));

        List<Permission> saved = permissionRepository.saveAll(permissions);
        log.info("Seeded {} permissions", saved.size());
        return saved;
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule(module);
        return permission;
    }

    // ═══════════════════════════════════════
    // Step 2: Companies (3 records)
    // ═══════════════════════════════════════

    private List<Company> seedCompanies() {
        List<Company> companies = List.of(
                createCompany("HoiDanIT", "Education platform", "Ha Noi"),
                createCompany("ABC Software", "IT outsourcing", "Ho Chi Minh City"),
                createCompany("Bla Bla Corporation", "Technology company", "Da Nang"));

        List<Company> saved = companyRepository.saveAll(companies);
        log.info("Seeded {} companies", saved.size());
        return saved;
    }

    private Company createCompany(String name, String description, String address) {
        Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        company.setAddress(address);
        return company;
    }

    // ═══════════════════════════════════════
    // Step 3: Roles (4 records)
    // ═══════════════════════════════════════

    private List<Role> seedRoles(List<Permission> allPermissions) {
        // SUPER_ADMIN — all permissions
        Role superAdmin = createRole("SUPER_ADMIN", "Full system access", allPermissions);

        // HR — user management + view company + upload file
        Role hr = createRole("HR", "Human resources management",
                filterPermissions(allPermissions,
                        "CREATE_USER", "UPDATE_USER", "VIEW_USERS", "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "UPLOAD_FILE"));

        // MANAGER — view only
        Role manager = createRole("MANAGER", "Department manager",
                filterPermissions(allPermissions,
                        "VIEW_USERS", "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "VIEW_ROLES", "VIEW_ROLE"));

        // USER — basic access
        Role user = createRole("USER", "Regular employee",
                filterPermissions(allPermissions,
                        "VIEW_USER",
                        "VIEW_COMPANIES", "VIEW_COMPANY",
                        "UPLOAD_FILE"));

        List<Role> saved = roleRepository.saveAll(List.of(superAdmin, hr, manager, user));
        log.info("Seeded {} roles", saved.size());
        return saved;
    }

    private Role createRole(String name, String description, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        return role;
    }

    private List<Permission> filterPermissions(List<Permission> all, String... names) {
        List<String> nameList = List.of(names);
        return all.stream()
                .filter(p -> nameList.contains(p.getName()))
                .toList();
    }

    // ═══════════════════════════════════════
    // Step 4: Users (4 records)
    // ═══════════════════════════════════════

    private void seedUsers(List<Role> allRoles, List<Company> allCompanies) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        Role superAdminRole = findRole(allRoles, "SUPER_ADMIN");
        Role hrRole = findRole(allRoles, "HR");
        Role managerRole = findRole(allRoles, "MANAGER");
        Role userRole = findRole(allRoles, "USER");

        Company hoidanit = findCompany(allCompanies, "HoiDanIT");
        Company abc = findCompany(allCompanies, "ABC Software");
        Company blabla = findCompany(allCompanies, "Bla Bla Corporation");

        List<User> users = List.of(
                createUser("Super Admin", "admin@example.com", encodedPassword,
                        30, GenderEnum.MALE, "Ho Chi Minh City",
                        hoidanit, List.of(superAdminRole)),

                createUser("HR Manager", "hr@example.com", encodedPassword,
                        28, GenderEnum.FEMALE, "Ha Noi",
                        abc, List.of(hrRole)),

                createUser("Department Manager", "manager@example.com", encodedPassword,
                        35, GenderEnum.MALE, "Da Nang",
                        blabla, List.of(managerRole)),

                createUser("Normal User", "user@example.com", encodedPassword,
                        25, GenderEnum.OTHER, "Ho Chi Minh City",
                        hoidanit, List.of(userRole)));

        userRepository.saveAll(users);
        log.info("Seeded {} users (password for all: {})", users.size(), DEFAULT_PASSWORD);
    }

    private User createUser(String name, String email, String encodedPassword,
            int age, GenderEnum gender, String address,
            Company company, List<Role> roles) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setAge(age);
        user.setGender(gender);
        user.setAddress(address);
        user.setCompany(company);
        user.setRoles(roles);
        return user;
    }

    private Role findRole(List<Role> roles, String name) {
        return roles.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    private Company findCompany(List<Company> companies, String name) {
        return companies.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company not found: " + name));
    }
}