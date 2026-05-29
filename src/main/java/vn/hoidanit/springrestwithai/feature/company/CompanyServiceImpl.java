package vn.hoidanit.springrestwithai.feature.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Công ty", "name", request.name());
        }

        Company company = new Company();
        company.setName(request.name());
        company.setDescription(request.description());
        company.setAddress(request.address());
        company.setLogo(request.logo());

        Company saved = companyRepository.save(company);
        return CompanyResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CompanyResponse update(UpdateCompanyRequest request) {
        Company company = companyRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", request.id()));

        if (companyRepository.existsByNameAndIdNot(request.name(), request.id())) {
            throw new DuplicateResourceException("Công ty", "name", request.name());
        }

        company.setName(request.name());
        company.setDescription(request.description());
        company.setAddress(request.address());
        company.setLogo(request.logo());

        Company saved = companyRepository.save(company);
        return CompanyResponse.fromEntity(saved);
    }

    @Override
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
        return CompanyResponse.fromEntity(company);
    }

    @Override
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<CompanyResponse> pageResult = companyRepository.findAll(pageable)
                .map(CompanyResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Công ty", "id", id);
        }
        companyRepository.deleteById(id);
    }
}
