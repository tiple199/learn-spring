package vn.hoidanit.springrestwithai.feature.user;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(Pageable pageable) {
        ResultPaginationDTO result = userService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        UserResponse user = this.userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        URI location = URI.create("/api/v1/users/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo người dùng thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> update(@Valid @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = this.userService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", updatedUser));
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }
}
