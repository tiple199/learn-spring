package vn.hoidanit.springrestwithai.feature.user;

import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;


public interface UserService {

    ResultPaginationDTO getAll(Pageable pageable);

    UserResponse getById(Long id);

    UserResponse create(CreateUserRequest request);

    UserResponse update(UpdateUserRequest request);

    void delete(Long id);
}
