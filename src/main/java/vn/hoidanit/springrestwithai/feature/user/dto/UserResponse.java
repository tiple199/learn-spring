package vn.hoidanit.springrestwithai.feature.user.dto;

import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        Integer age,
        String address,
        GenderEnum gender,
        String avatar,
        CompanyInfo company,
        List<RoleInfo> roles,
        Instant createdAt,
        Instant updatedAt) {
    public record CompanyInfo(Long id, String name) {
    }

    public record RoleInfo(Long id, String name) {
    }

    public static UserResponse fromEntity(User user) {
        CompanyInfo companyInfo = user.getCompany() != null
                ? new CompanyInfo(user.getCompany().getId(), user.getCompany().getName())
                : null;

        List<RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> new RoleInfo(role.getId(), role.getName()))
                .toList();

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getAddress(),
                user.getGender(),
                user.getAvatar(),
                companyInfo,
                roleInfos,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
