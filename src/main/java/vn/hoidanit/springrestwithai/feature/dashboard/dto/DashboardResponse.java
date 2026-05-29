package vn.hoidanit.springrestwithai.feature.dashboard.dto;

public record DashboardResponse(
        long totalUsers,
        long totalCompanies,
        long totalRoles,
        long totalPermissions) {
}
