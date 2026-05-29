package vn.hoidanit.springrestwithai.feature.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    // ========== create ==========

    @Test
    @DisplayName("create - success: returns CompanyResponse and saves once")
    void create_success_returnsCompanyResponse() {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", "Education platform", "Ho Chi Minh City", "/logos/hoidanit.png");

        Company saved = buildCompany(1L, "HoiDanIT", "Education platform",
                "Ho Chi Minh City", "/logos/hoidanit.png");

        when(companyRepository.existsByName("HoiDanIT")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(saved);

        CompanyResponse response = companyService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("HoiDanIT");
        assertThat(response.description()).isEqualTo("Education platform");
        assertThat(response.address()).isEqualTo("Ho Chi Minh City");
        assertThat(response.logo()).isEqualTo("/logos/hoidanit.png");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("create - duplicate name: throws DuplicateResourceException")
    void create_duplicateName_throwsDuplicateResourceException() {
        CreateCompanyRequest request = new CreateCompanyRequest(
                "HoiDanIT", null, null, null);

        when(companyRepository.existsByName("HoiDanIT")).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(companyRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update - success: updates fields and returns CompanyResponse")
    void update_success_returnsUpdatedCompanyResponse() {
        Company existing = buildCompany(1L, "Old Name", "Old desc", "Old addr", null);
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                1L, "New Name", "New desc", "New addr", "/logos/new.png");
        Company updated = buildCompany(1L, "New Name", "New desc", "New addr", "/logos/new.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(companyRepository.existsByNameAndIdNot("New Name", 1L)).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(updated);

        CompanyResponse response = companyService.update(request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New desc");
        assertThat(response.address()).isEqualTo("New addr");
        assertThat(response.logo()).isEqualTo("/logos/new.png");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("update - not found: throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                99L, "Some Name", null, null, null);

        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.update(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - duplicate name for different company: throws DuplicateResourceException")
    void update_duplicateNameForOtherId_throwsDuplicateResourceException() {
        Company existing = buildCompany(1L, "HoiDanIT", null, null, null);
        UpdateCompanyRequest request = new UpdateCompanyRequest(
                1L, "FPT Software", null, null, null);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(companyRepository.existsByNameAndIdNot("FPT Software", 1L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.update(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(companyRepository, never()).save(any());
    }

    // ========== getById ==========

    @Test
    @DisplayName("getById - found: returns CompanyResponse")
    void getById_found_returnsCompanyResponse() {
        Company company = buildCompany(1L, "HoiDanIT", null, null, null);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("HoiDanIT");
    }

    @Test
    @DisplayName("getById - not found: throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getAll ==========

    @Test
    @DisplayName("getAll - returns page with companies")
    void getAll_returnsPageOfCompanyResponse() {
        Company c1 = buildCompany(1L, "HoiDanIT", null, null, null);
        Company c2 = buildCompany(2L, "FPT Software", null, null, null);
        Page<Company> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);

        when(companyRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        ResultPaginationDTO result = companyService.getAll(PageRequest.of(0, 10));

        assertThat(result.meta().total()).isEqualTo(2);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.result()).hasSize(2);
    }

    @Test
    @DisplayName("getAll - empty database: returns empty page")
    void getAll_emptyDatabase_returnsEmptyPage() {
        Page<Company> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(companyRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        ResultPaginationDTO result = companyService.getAll(PageRequest.of(0, 10));

        assertThat(result.result()).isEmpty();
        assertThat(result.meta().total()).isZero();
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete - success: calls deleteById once")
    void delete_success_callsDeleteById() {
        when(companyRepository.existsById(1L)).thenReturn(true);

        companyService.delete(1L);

        verify(companyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - not found: throws ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(companyRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> companyService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyRepository, never()).deleteById(anyLong());
    }

    // ========== helpers ==========

    private Company buildCompany(Long id, String name, String description,
            String address, String logo) {
        Company c = new Company();
        c.setId(id);
        c.setName(name);
        c.setDescription(description);
        c.setAddress(address);
        c.setLogo(logo);
        return c;
    }
}
