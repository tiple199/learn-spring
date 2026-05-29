package vn.hoidanit.springrestwithai.feature.company;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = companyService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công ty thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getById(@PathVariable Long id) {
        CompanyResponse response = companyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin công ty thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(
            @Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyService.create(request);
        URI location = URI.create("/api/v1/companies/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo công ty thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> update(
            @Valid @RequestBody UpdateCompanyRequest request) {
        CompanyResponse response = companyService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật công ty thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công ty thành công", null));
    }
}
