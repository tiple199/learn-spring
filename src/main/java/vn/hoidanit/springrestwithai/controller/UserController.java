package vn.hoidanit.springrestwithai.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.helper.ApiResponse;
import vn.hoidanit.springrestwithai.model.User;
import vn.hoidanit.springrestwithai.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users - lấy tất cả users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = this.userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
    }

    // GET /users/{id} - lấy user theo id
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Không tìm thấy người dùng với id = " + id));
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", user));
    }

    // POST /users - tạo user mới
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        if (this.userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.conflict("Email '" + user.getEmail() + "' đã được sử dụng"));
        }
        User createdUser = this.userService.createUser(user);
        URI location = URI.create("/users/" + createdUser.getId());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo người dùng mới thành công", createdUser));
    }

    // PUT /users/{id} - cập nhật user
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = this.userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Không tìm thấy người dùng với id = " + id));
        }
        user.setId(id);
        User updatedUser = this.userService.updateUser(user);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", updatedUser));
    }

    // DELETE /users/{id} - xóa user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User existingUser = this.userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Không tìm thấy người dùng với id = " + id));
        }
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
