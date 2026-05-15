package vn.hoidanit.springrestwithai.feature.permission;

import org.springframework.data.domain.Page;

import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

public interface PermissionService {

    PermissionResponse create(CreatePermissionRequest request);

    PermissionResponse update(UpdatePermissionRequest request);

    PermissionResponse getById(Long id);

    Page<PermissionResponse> getAll(int page, int size);

    void delete(Long id);
}
