package vn.hoidanit.springrestwithai.feature.user;

import org.springframework.data.domain.Page;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;


public interface UserService {

    Page<UserResponse> getAll(int page, int size);

    UserResponse getById(Long id);

    UserResponse create(CreateUserRequest request);

    UserResponse update(UpdateUserRequest request);

    void delete(Long id);
}
