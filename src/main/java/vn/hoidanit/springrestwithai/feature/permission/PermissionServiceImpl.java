package vn.hoidanit.springrestwithai.feature.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.permission.dto.CreatePermissionRequest;
import vn.hoidanit.springrestwithai.feature.permission.dto.PermissionResponse;
import vn.hoidanit.springrestwithai.feature.permission.dto.UpdatePermissionRequest;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByApiPathAndMethod(request.apiPath(), request.method())) {
            throw new DuplicateResourceException("Quyền hạn", "apiPath + method",
                    request.apiPath() + " [" + request.method() + "]");
        }

        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission saved = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public PermissionResponse update(UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn", "id", request.id()));

        if (permissionRepository.existsByApiPathAndMethodAndIdNot(
                request.apiPath(), request.method(), request.id())) {
            throw new DuplicateResourceException("Quyền hạn", "apiPath + method",
                    request.apiPath() + " [" + request.method() + "]");
        }

        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission saved = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(saved);
    }

    @Override
    public PermissionResponse getById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn", "id", id));
        return PermissionResponse.fromEntity(permission);
    }

    @Override
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<PermissionResponse> pageResult = permissionRepository.findAll(pageable)
                .map(PermissionResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quyền hạn", "id", id);
        }
        permissionRepository.deleteById(id);
    }
}
