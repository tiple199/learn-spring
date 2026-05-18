package vn.hoidanit.springrestwithai.feature.company;


import org.springframework.data.domain.Page;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

public interface CompanyService {
    CompanyResponse create(CreateCompanyRequest request);

    CompanyResponse update(UpdateCompanyRequest request);

    CompanyResponse getById(Long id);

    Page<CompanyResponse> getAll(int page, int size);

    void delete(Long id);
}
