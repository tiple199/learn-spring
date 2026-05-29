package vn.hoidanit.springrestwithai.feature.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCompanyRequest(
        @NotNull(message = "ID không được để trống") Long id,
        @NotBlank(message = "Tên công ty không được để trống") String name,
        String description,
        String address,
        String logo
) {
}
