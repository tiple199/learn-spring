package vn.hoidanit.springrestwithai.feature.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.dashboard.dto.DashboardResponse;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("getDashboard - success: returns totals from repositories")
    void getDashboard_success_returnsTotalsFromRepositories() {
        when(userRepository.count()).thenReturn(10L);
        when(companyRepository.count()).thenReturn(3L);
        when(roleRepository.count()).thenReturn(4L);
        when(permissionRepository.count()).thenReturn(25L);

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalUsers()).isEqualTo(10L);
        assertThat(response.totalCompanies()).isEqualTo(3L);
        assertThat(response.totalRoles()).isEqualTo(4L);
        assertThat(response.totalPermissions()).isEqualTo(25L);
        verify(userRepository, times(1)).count();
        verify(companyRepository, times(1)).count();
        verify(roleRepository, times(1)).count();
        verify(permissionRepository, times(1)).count();
    }

    @Test
    @DisplayName("getDashboard - empty database: returns zero totals")
    void getDashboard_emptyDatabase_returnsZeroTotals() {
        when(userRepository.count()).thenReturn(0L);
        when(companyRepository.count()).thenReturn(0L);
        when(roleRepository.count()).thenReturn(0L);
        when(permissionRepository.count()).thenReturn(0L);

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalUsers()).isZero();
        assertThat(response.totalCompanies()).isZero();
        assertThat(response.totalRoles()).isZero();
        assertThat(response.totalPermissions()).isZero();
    }
}
