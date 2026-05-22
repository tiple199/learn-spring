package vn.hoidanit.springrestwithai.feature.role;

import org.springframework.data.domain.Page;

import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.RoleResponse;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    RoleResponse update(UpdateRoleRequest request);

    RoleResponse getById(Long id);

    Page<RoleResponse> getAll(int page, int size);

    void delete(Long id);
    
} 