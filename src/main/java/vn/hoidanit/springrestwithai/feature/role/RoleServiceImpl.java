package vn.hoidanit.springrestwithai.feature.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.permission.PermissionRepository;
import vn.hoidanit.springrestwithai.feature.role.dto.CreateRoleRequest;
import vn.hoidanit.springrestwithai.feature.role.dto.RoleResponse;
import vn.hoidanit.springrestwithai.feature.role.dto.UpdateRoleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Vai trò", "name", request.name());
        }

        List<Permission> permissions = resolvePermissions(request.permissionIds());

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        return RoleResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RoleResponse update(UpdateRoleRequest request) {
        Role role = roleRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò", "id", request.id()));

        if (roleRepository.existsByNameAndIdNot(request.name(), request.id())) {
            throw new DuplicateResourceException("Vai trò", "name", request.name());
        }

        List<Permission> permissions = resolvePermissions(request.permissionIds());

        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        return RoleResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RoleResponse getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò", "id", id));
        return RoleResponse.fromEntity(role);
    }

    @Override
    @Transactional
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<RoleResponse> pageResult = roleRepository.findAll(pageable)
                .map(RoleResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vai trò", "id", id);
        }
        roleRepository.deleteById(id);
    }

    private List<Permission> resolvePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new ArrayList<>(); // mutable — Hibernate có thể thao tác được
        }

        List<Long> uniqueIds = permissionIds.stream().distinct().toList();
        List<Permission> found = permissionRepository.findAllById(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            Set<Long> foundIds = found.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());
            Long missingId = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ResourceNotFoundException("Quyền hạn", "id", missingId);
        }

        return found;
    }
}
