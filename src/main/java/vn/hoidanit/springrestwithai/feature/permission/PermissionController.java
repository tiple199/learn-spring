package vn.hoidanit.springrestwithai.feature.permission;

import jakarta.validation.Valid;
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
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = permissionService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quyền hạn thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quyền hạn thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.create(request);
        URI location = URI.create("/api/v1/permissions/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo quyền hạn thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionResponse response = permissionService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quyền hạn thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa quyền hạn thành công", null));
    }
}
