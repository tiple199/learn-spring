package vn.hoidanit.springrestwithai.feature.user;

import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserFilterRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse update(UpdateUserRequest request);

    UserResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    ResultPaginationDTO filter(UserFilterRequest filter, Pageable pageable);

    void delete(Long id);
}
