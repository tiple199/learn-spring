package vn.hoidanit.springrestwithai.feature.company;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CompanyResponse;
import vn.hoidanit.springrestwithai.feature.company.dto.CreateCompanyRequest;
import vn.hoidanit.springrestwithai.feature.company.dto.UpdateCompanyRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<CompanyResponse> result = companyService.getAll(page,size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công ty thành công",result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getById(@PathVariable Long id){
        CompanyResponse result = companyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin công ty thành công",result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CreateCompanyRequest createCompanyRequest){
        CompanyResponse result = companyService.create(createCompanyRequest);
        URI location = URI.create("/api/v1/companies/" + result.id());
        return ResponseEntity.created(location).body(ApiResponse.created("Tạo công ty thành công",result));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> update(@Valid @RequestBody UpdateCompanyRequest updateCompanyRequest){
        CompanyResponse result = companyService.update(updateCompanyRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật công ty thành công",result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> delete(@PathVariable Long id){
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công ty thành công", null));
    }




}


