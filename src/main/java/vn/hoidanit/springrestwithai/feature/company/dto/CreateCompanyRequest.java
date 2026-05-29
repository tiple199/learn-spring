package vn.hoidanit.springrestwithai.feature.company.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
        @NotBlank(message = "Tên công ty không được để trống") String name,
        String description,
        String address,
        String logo
) {
}
