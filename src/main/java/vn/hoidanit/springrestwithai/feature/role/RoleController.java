package vn.hoidanit.springrestwithai.feature.role;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.RoleResponse;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = roleService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable Long id) {
        RoleResponse response = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vai trò thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.create(request);
        URI location = URI.create("/api/v1/roles/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo vai trò thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse response = roleService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vai trò thành công", null));
    }
}
