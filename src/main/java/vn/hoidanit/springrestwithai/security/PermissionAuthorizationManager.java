package vn.hoidanit.springrestwithai.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;

@Component
public class PermissionAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final RoleRepository roleRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // Dùng để match path với wildcard

    // Cache: roleName → List<Permission>
    // Load 1 lần, invalidate khi admin thay đổi
    private Map<String, List<Permission>> rolePermissionsCache;

    public PermissionAuthorizationManager(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        loadCache();
    }

    public void loadCache() {
        // 1 query JOIN FETCH tất cả roles + permissions
        List<Role> roles = roleRepository.findAllWithPermissions();

        Map<String, List<Permission>> cache = new HashMap<>();
        for (Role role : roles) {
            cache.computeIfAbsent("ROLE_" + role.getName(), k -> new ArrayList<>())
                    .addAll(role.getPermissions());
        }
        this.rolePermissionsCache = cache;
    }

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authSupplier,
            RequestAuthorizationContext context) {

        Authentication authentication = authSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        String requestPath = context.getRequest().getRequestURI();
        String httpMethod = context.getRequest().getMethod();

        // Lấy roles của user từ JWT
        List<String> userRoles = getUserRolesFromJwt(authentication);

        // Với mỗi role → lấy permissions từ cache → check match
        for (String role : userRoles) {
            List<Permission> permissions = rolePermissionsCache
                    .getOrDefault(role, Collections.emptyList());

            for (Permission perm : permissions) {
                if (perm.getMethod().equalsIgnoreCase(httpMethod)
                        && pathMatcher.match(perm.getApiPath(), requestPath)) {
                    return new AuthorizationDecision(true);
                }
            }
        }

        return new AuthorizationDecision(false);
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserRolesFromJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Object rolesClaim = jwtToken.getToken().getClaim("roles");
            if (rolesClaim instanceof List<?> roles) {
                return (List<String>) roles;
            }
        }
        return Collections.emptyList();
    }
}
