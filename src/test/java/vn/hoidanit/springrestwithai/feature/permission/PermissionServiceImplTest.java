package vn.hoidanit.springrestwithai.feature.permission;

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
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

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
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    // ========== create ==========

    @Test
    @DisplayName("create - success: returns PermissionResponse and saves once")
    void create_success_returnsPermissionResponse() {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        Permission saved = buildPermission(1L, "CREATE_USER", "/api/v1/users", "POST", "USER");

        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(saved);

        PermissionResponse response = permissionService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("CREATE_USER");
        assertThat(response.apiPath()).isEqualTo("/api/v1/users");
        assertThat(response.method()).isEqualTo("POST");
        assertThat(response.module()).isEqualTo("USER");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("create - duplicate apiPath+method: throws DuplicateResourceException")
    void create_duplicateApiPathAndMethod_throwsDuplicateResourceException() {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "CREATE_USER", "/api/v1/users", "POST", "USER");

        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(permissionRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update - success: updates fields and returns PermissionResponse")
    void update_success_returnsUpdatedPermissionResponse() {
        Permission existing = buildPermission(1L, "OLD_NAME", "/api/v1/old", "GET", "USER");
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                1L, "NEW_NAME", "/api/v1/new", "PUT", "ROLE");
        Permission updated = buildPermission(1L, "NEW_NAME", "/api/v1/new", "PUT", "ROLE");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByApiPathAndMethodAndIdNot("/api/v1/new", "PUT", 1L)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(updated);

        PermissionResponse response = permissionService.update(request);

        assertThat(response.name()).isEqualTo("NEW_NAME");
        assertThat(response.apiPath()).isEqualTo("/api/v1/new");
        assertThat(response.method()).isEqualTo("PUT");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("update - not found: throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                99L, "NAME", "/api/v1/path", "GET", "USER");

        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.update(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - duplicate apiPath+method for different permission: throws DuplicateResourceException")
    void update_duplicateApiPathAndMethodForOtherId_throwsDuplicateResourceException() {
        Permission existing = buildPermission(1L, "CREATE_USER", "/api/v1/users", "POST", "USER");
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                1L, "CREATE_USER", "/api/v1/companies", "POST", "COMPANY");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByApiPathAndMethodAndIdNot("/api/v1/companies", "POST", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> permissionService.update(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(permissionRepository, never()).save(any());
    }

    // ========== getById ==========

    @Test
    @DisplayName("getById - found: returns PermissionResponse")
    void getById_found_returnsPermissionResponse() {
        Permission permission = buildPermission(1L, "CREATE_USER", "/api/v1/users", "POST", "USER");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        PermissionResponse response = permissionService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("CREATE_USER");
    }

    @Test
    @DisplayName("getById - not found: throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getAll ==========

    @Test
    @DisplayName("getAll - returns page with permissions")
    void getAll_returnsPageOfPermissionResponse() {
        Permission p1 = buildPermission(1L, "CREATE_USER", "/api/v1/users", "POST", "USER");
        Permission p2 = buildPermission(2L, "DELETE_USER", "/api/v1/users/1", "DELETE", "USER");
        Page<Permission> page = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10), 2);

        when(permissionRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        ResultPaginationDTO result = permissionService.getAll(PageRequest.of(0, 10));

        assertThat(result.meta().total()).isEqualTo(2);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.result()).hasSize(2);
    }

    @Test
    @DisplayName("getAll - empty database: returns empty page")
    void getAll_emptyDatabase_returnsEmptyPage() {
        Page<Permission> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(permissionRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        ResultPaginationDTO result = permissionService.getAll(PageRequest.of(0, 10));

        assertThat(result.result()).isEmpty();
        assertThat(result.meta().total()).isZero();
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete - success: calls deleteById once")
    void delete_success_callsDeleteById() {
        when(permissionRepository.existsById(1L)).thenReturn(true);

        permissionService.delete(1L);

        verify(permissionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - not found: throws ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(permissionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> permissionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(permissionRepository, never()).deleteById(anyLong());
    }

    // ========== helpers ==========

    private Permission buildPermission(Long id, String name, String apiPath, String method, String module) {
        Permission p = new Permission();
        p.setId(id);
        p.setName(name);
        p.setApiPath(apiPath);
        p.setMethod(method);
        p.setModule(module);
        return p;
    }
}
