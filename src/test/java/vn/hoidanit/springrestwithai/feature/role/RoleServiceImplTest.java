package vn.hoidanit.springrestwithai.feature.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.RoleResponse;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    // ========== create ==========

    @Test
    @DisplayName("create - success: returns RoleResponse and saves once")
    void create_success_returnsRoleResponse() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Full access", List.of(1L));
        Permission p1 = buildPermission(1L, "CREATE_USER");
        Role saved = buildRole(1L, "ADMIN", "Full access", List.of(p1));

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L))).thenReturn(List.of(p1));
        when(roleRepository.save(any(Role.class))).thenReturn(saved);

        RoleResponse response = roleService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("ADMIN");
        assertThat(response.description()).isEqualTo("Full access");
        assertThat(response.permissions()).hasSize(1);
        assertThat(response.permissions().get(0).name()).isEqualTo("CREATE_USER");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("create - duplicate name: throws DuplicateResourceException")
    void create_duplicateName_throwsDuplicateResourceException() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of());

        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - empty permissionIds: saves role with no permissions")
    void create_withEmptyPermissionIds_returnsRoleWithNoPermissions() {
        CreateRoleRequest request = new CreateRoleRequest("VIEWER", "Read only", List.of());
        Role saved = buildRole(2L, "VIEWER", "Read only", List.of());

        when(roleRepository.existsByName("VIEWER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(saved);

        RoleResponse response = roleService.create(request);

        assertThat(response.permissions()).isEmpty();
        verify(permissionRepository, never()).findAllById(any());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("create - missing permissionId: throws ResourceNotFoundException for Quyền hạn")
    void create_withMissingPermissionId_throwsResourceNotFoundException() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc", List.of(1L, 99L));
        Permission p1 = buildPermission(1L, "CREATE_USER");

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(p1));

        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quyền hạn");

        verify(roleRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update - success: updates fields and returns RoleResponse")
    void update_success_returnsUpdatedRoleResponse() {
        Role existing = buildRole(1L, "OLD_NAME", "old desc", new ArrayList<>());
        Permission p1 = buildPermission(1L, "CREATE_USER");
        UpdateRoleRequest request = new UpdateRoleRequest(1L, "NEW_NAME", "new desc", List.of(1L));
        Role updated = buildRole(1L, "NEW_NAME", "new desc", List.of(p1));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameAndIdNot("NEW_NAME", 1L)).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L))).thenReturn(List.of(p1));
        when(roleRepository.save(any(Role.class))).thenReturn(updated);

        RoleResponse response = roleService.update(request);

        assertThat(response.name()).isEqualTo("NEW_NAME");
        assertThat(response.description()).isEqualTo("new desc");
        assertThat(response.permissions()).hasSize(1);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("update - not found: throws ResourceNotFoundException for Vai trò")
    void update_notFound_throwsResourceNotFoundException() {
        UpdateRoleRequest request = new UpdateRoleRequest(99L, "NAME", "desc", List.of());

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vai trò");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - duplicate name for other role: throws DuplicateResourceException")
    void update_duplicateNameForOtherId_throwsDuplicateResourceException() {
        Role existing = buildRole(1L, "ADMIN", "desc", new ArrayList<>());
        UpdateRoleRequest request = new UpdateRoleRequest(1L, "MANAGER", "desc", List.of());

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameAndIdNot("MANAGER", 1L)).thenReturn(true);

        assertThatThrownBy(() -> roleService.update(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - missing permissionId: throws ResourceNotFoundException for Quyền hạn")
    void update_withMissingPermissionId_throwsResourceNotFoundException() {
        Role existing = buildRole(1L, "ADMIN", "desc", new ArrayList<>());
        UpdateRoleRequest request = new UpdateRoleRequest(1L, "ADMIN", "desc", List.of(1L, 99L));
        Permission p1 = buildPermission(1L, "CREATE_USER");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByNameAndIdNot("ADMIN", 1L)).thenReturn(false);
        when(permissionRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(p1));

        assertThatThrownBy(() -> roleService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quyền hạn");

        verify(roleRepository, never()).save(any());
    }

    // ========== getById ==========

    @Test
    @DisplayName("getById - found: returns RoleResponse")
    void getById_found_returnsRoleResponse() {
        Role role = buildRole(1L, "ADMIN", "Full access", List.of());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleResponse response = roleService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("getById - not found: throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getAll ==========

    @Test
    @DisplayName("getAll - returns page with roles")
    void getAll_returnsPageOfRoleResponse() {
        Role r1 = buildRole(1L, "ADMIN", "Full access", List.of());
        Role r2 = buildRole(2L, "VIEWER", "Read only", List.of());
        Page<Role> page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 10), 2);

        when(roleRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        ResultPaginationDTO result = roleService.getAll(PageRequest.of(0, 10));

        assertThat(result.meta().total()).isEqualTo(2);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.result()).hasSize(2);
    }

    @Test
    @DisplayName("getAll - empty database: returns empty page")
    void getAll_emptyDatabase_returnsEmptyPage() {
        Page<Role> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(roleRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        ResultPaginationDTO result = roleService.getAll(PageRequest.of(0, 10));

        assertThat(result.result()).isEmpty();
        assertThat(result.meta().total()).isZero();
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete - success: calls deleteById once")
    void delete_success_callsDeleteById() {
        when(roleRepository.existsById(1L)).thenReturn(true);

        roleService.delete(1L);

        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - not found: throws ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(roleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roleRepository, never()).deleteById(anyLong());
    }

    // ========== helpers ==========

    private Role buildRole(Long id, String name, String description, List<Permission> permissions) {
        Role r = new Role();
        r.setId(id);
        r.setName(name);
        r.setDescription(description);
        r.setPermissions(new ArrayList<>(permissions));
        return r;
    }

    private Permission buildPermission(Long id, String name) {
        Permission p = new Permission();
        p.setId(id);
        p.setName(name);
        p.setApiPath("/api/v1/test");
        p.setMethod("GET");
        p.setModule("TEST");
        return p;
    }
}
