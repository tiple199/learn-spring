package vn.hoidanit.springrestwithai.feature.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionRequest(
        @NotNull(message = "ID không được để trống") Long id,
        @NotBlank(message = "Tên quyền không được để trống") String name,
        @NotBlank(message = "API path không được để trống") String apiPath,
        @NotBlank(message = "Phương thức HTTP không được để trống") String method,
        @NotBlank(message = "Module không được để trống") String module
) {
}
