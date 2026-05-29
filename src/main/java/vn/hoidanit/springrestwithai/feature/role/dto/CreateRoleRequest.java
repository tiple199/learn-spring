package vn.hoidanit.springrestwithai.feature.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRoleRequest(
        @NotBlank(message = "Tên vai trò không được để trống") String name,
        String description,
        @NotNull(message = "Danh sách quyền hạn không được null") List<Long> permissionIds
) {
}
