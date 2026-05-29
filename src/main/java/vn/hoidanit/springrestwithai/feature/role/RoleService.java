package vn.hoidanit.springrestwithai.feature.role;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.RoleResponse;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    RoleResponse update(UpdateRoleRequest request);

    RoleResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);
}
