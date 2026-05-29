package vn.hoidanit.springrestwithai.feature.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // ========== CREATE ==========

    @Test
    @DisplayName("create: valid request without company/roles → returns UserResponse")
    void create_validRequestNoCompanyNoRoles_returnsUserResponse() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A", "a@example.com", "password123",
                25, "Hanoi", GenderEnum.MALE, "/avatars/a.png", null, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertThat(response.email()).isEqualTo("a@example.com");
        assertThat(response.name()).isEqualTo("Nguyen Van A");
        assertThat(response.avatar()).isEqualTo("/avatars/a.png");
        assertThat(response.company()).isNull();
        assertThat(response.roles()).isEmpty();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("create: valid request with company and roles → returns UserResponse with associations")
    void create_withCompanyAndRoles_returnsUserResponseWithAssociations() {
        Company company = buildCompany(10L, "Tech Corp");
        Role role = buildRole(20L, "ADMIN");

        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van B", "b@example.com", "password123",
                30, "HCM", GenderEnum.FEMALE, "/avatars/b.png", 10L, List.of(20L));

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(roleRepository.findAllById(List.of(20L))).thenReturn(List.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertThat(response.avatar()).isEqualTo("/avatars/b.png");
        assertThat(response.company()).isNotNull();
        assertThat(response.company().id()).isEqualTo(10L);
        assertThat(response.roles()).hasSize(1);
        assertThat(response.roles().get(0).id()).isEqualTo(20L);
    }

    @Test
    @DisplayName("create: email already exists → throws DuplicateResourceException")
    void create_emailAlreadyExists_throwsDuplicateResourceException() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van A", "a@example.com", "password123",
                25, null, null, null, null, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("a@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: companyId not found → throws ResourceNotFoundException")
    void create_companyNotFound_throwsResourceNotFoundException() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van C", "c@example.com", "password123",
                25, null, null, null, 99L, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: roleId not found → throws ResourceNotFoundException")
    void create_roleNotFound_throwsResourceNotFoundException() {
        CreateUserRequest request = new CreateUserRequest(
                "Nguyen Van D", "d@example.com", "password123",
                25, null, null, null, null, List.of(999L));

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findAllById(List.of(999L))).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    // ========== GET BY ID ==========

    @Test
    @DisplayName("getById: existing id → returns UserResponse")
    void getById_existingId_returnsUserResponse() {
        User user = buildUser(1L, "Nguyen Van A", "a@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@example.com");
    }

    @Test
    @DisplayName("getById: non-existing id → throws ResourceNotFoundException")
    void getById_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ========== GET ALL ==========

    @Test
    @DisplayName("getAll: returns paged result with meta")
    void getAll_returnsPagedResult() {
        List<User> users = List.of(
                buildUser(1L, "User A", "a@example.com"),
                buildUser(2L, "User B", "b@example.com"));
        Page<User> page = new PageImpl<>(users);
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        ResultPaginationDTO result = userService.getAll(PageRequest.of(0, 10));

        assertThat(result.meta().total()).isEqualTo(2);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.result()).hasSize(2);
    }

    // ========== UPDATE ==========

    @Test
    @DisplayName("update: valid request → returns updated UserResponse")
    void update_validRequest_returnsUpdatedUserResponse() {
        User existing = buildUser(1L, "Old Name", "old@example.com");
        UpdateUserRequest request = new UpdateUserRequest(
                1L, "New Name", "new@example.com", 30, "HCM",
                GenderEnum.MALE, "/avatars/new.png", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.avatar()).isEqualTo("/avatars/new.png");
    }

    @Test
    @DisplayName("update: user not found → throws ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest(
                999L, "Name", "e@example.com", null, null, null, null, null, null);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("update: email taken by another user → throws DuplicateResourceException")
    void update_emailTakenByOther_throwsDuplicateResourceException() {
        User existing = buildUser(1L, "User A", "a@example.com");
        UpdateUserRequest request = new UpdateUserRequest(
                1L, "User A", "taken@example.com", null, null, null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
    }

    // ========== DELETE ==========

    @Test
    @DisplayName("delete: existing id → calls deleteById")
    void delete_existingId_callsDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: non-existing id → throws ResourceNotFoundException")
    void delete_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).deleteById(anyLong());
    }

    // ========== HELPERS ==========

    private User buildUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles(new ArrayList<>());
        return user;
    }

    private Company buildCompany(Long id, String name) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        return company;
    }

    private Role buildRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
