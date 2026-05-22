package vn.hoidanit.springrestwithai.feature.role.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoleRequest(
        @NotBlank(message = "Tên role không được để trống") String name,
        String description,
        @NotNull(message = "Danh sách quyền hạn không được rỗng") List<Long> permissionIds
) {
    
}
