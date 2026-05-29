package vn.hoidanit.springrestwithai.feature.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.company.Company;
import vn.hoidanit.springrestwithai.feature.company.CompanyRepository;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;
import vn.hoidanit.springrestwithai.feature.user.dto.CreateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UpdateUserRequest;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Người dùng", "email", request.email());
        }

        Company company = resolveCompany(request.companyId());
        List<Role> roles = resolveRoles(request.roleIds());

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setGender(request.gender());
        user.setCompany(company);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public UserResponse update(UpdateUserRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.id()));

        if (userRepository.existsByEmailAndIdNot(request.email(), request.id())) {
            throw new DuplicateResourceException("Người dùng", "email", request.email());
        }

        Company company = resolveCompany(request.companyId());
        List<Role> roles = resolveRoles(request.roleIds());

        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setGender(request.gender());
        user.setCompany(company);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<UserResponse> pageResult = userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Người dùng", "id", id);
        }
        userRepository.deleteById(id);
    }

    private Company resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", companyId));
    }

    private List<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> uniqueIds = roleIds.stream().distinct().toList();
        List<Role> found = roleRepository.findAllById(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            Set<Long> foundIds = found.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Long missingId = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ResourceNotFoundException("Vai trò", "id", missingId);
        }

        return found;
    }
}
