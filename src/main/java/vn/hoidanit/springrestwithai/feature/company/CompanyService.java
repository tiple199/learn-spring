package vn.hoidanit.springrestwithai.feature.company;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

public interface CompanyService {

    CompanyResponse create(CreateCompanyRequest request);

    CompanyResponse update(UpdateCompanyRequest request);

    CompanyResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);
}
