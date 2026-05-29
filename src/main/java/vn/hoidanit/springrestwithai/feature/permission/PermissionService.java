package vn.hoidanit.springrestwithai.feature.permission;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;
import org.springframework.data.domain.Pageable;

public interface PermissionService {

    PermissionResponse create(CreatePermissionRequest request);

    PermissionResponse update(UpdatePermissionRequest request);

    PermissionResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);
}
