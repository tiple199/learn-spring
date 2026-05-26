package vn.hoidanit.springrestwithai.feature.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository,CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public Page<UserResponse> getAll(int page, int size) {

        return this.userRepository.findAll(PageRequest.of(page, size))
                .map(UserResponse::fromEntity);
    }

    @Override
    public UserResponse getById(Long id) {
        return this.userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Người dùng", "email", request.email());
        }
        List<Role> roles = resolveRoles(request.roleIds());
        Company company = resolveCompany(request.companyId());
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setGender(request.gender());
        user.setCompany(company);
        user.setRoles(roles);
        User savedUser = this.userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(UpdateUserRequest request) {
        User user = this.userRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", request.id()));
        if(userRepository.existsByEmailAndIdNot(request.email(), request.id())){
            throw new DuplicateResourceException("Người dùng", "email", request.email());
        }
        List<Role> roles = resolveRoles(request.roleIds());
        Company company = resolveCompany(request.companyId());
        user.setId(request.id());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setCompany(company);
        user.setRoles(roles);
        user.setGender(request.gender());
        User savedUser = this.userRepository.save(user);
        return UserResponse.fromEntity((savedUser));
    }

    @Override
    public void delete(Long id) {
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("Người dùng", "id", id);
        }
        this.userRepository.deleteById(id);
    }

    private Company resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", companyId));
    }

    private List<Role> resolveRoles(List<Long> roleIds){
        if(roleIds == null || roleIds.isEmpty()){
            return new ArrayList<>();
        }

        List<Long> uniqueIds = roleIds.stream().distinct().toList();
        List<Role> roles = roleRepository.findAllById(uniqueIds);

        if(roles.size() != uniqueIds.size()){
            Set<Long> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Long missingId = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ResourceNotFoundException("Vai trò", "id", missingId);
        }

        return roles;
    }


}
