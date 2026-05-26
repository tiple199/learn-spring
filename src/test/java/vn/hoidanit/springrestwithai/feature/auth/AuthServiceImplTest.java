package vn.hoidanit.springrestwithai.feature.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.InvalidTokenException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest;
import vn.hoidanit.springrestwithai.feature.auth.dto.LoginResponse;
import vn.hoidanit.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.hoidanit.springrestwithai.feature.auth.dto.RegisterResponse;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }

    // ========== login ==========

    @Test
    @DisplayName("login - valid credentials: returns LoginResponse with tokens")
    void login_validCredentials_returnsLoginResponse() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = buildUser(1L, "test@example.com", "Test User");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(buildMockJwt("mock.token"));

        LoginResponse response = authService.login(request, "Mozilla/5.0", "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("mock.token");
        assertThat(response.refreshToken()).isEqualTo("mock.token");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("login - invalid credentials: throws BadCredentialsException")
    void login_invalidCredentials_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "Mozilla/5.0", "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    // ========== register ==========

    @Test
    @DisplayName("register - new email: returns RegisterResponse")
    void register_newEmail_returnsRegisterResponse() {
        RegisterRequest request = new RegisterRequest(
                "Test User", "new@example.com", "password123", 25, GenderEnum.MALE, "HCM");

        User saved = buildUser(1L, "new@example.com", "Test User");
        saved.setAge(25);
        saved.setGender(GenderEnum.MALE);
        saved.setAddress("HCM");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.age()).isEqualTo(25);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register - duplicate email: throws DuplicateResourceException")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest(
                "Test User", "existing@example.com", "password123", 25, GenderEnum.MALE, "HCM");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // ========== refresh ==========

    @Test
    @DisplayName("refresh - valid token: returns new LoginResponse and revokes old token")
    void refresh_validToken_returnsNewLoginResponse() {
        String rawToken = "valid.refresh.token";
        User user = buildUser(1L, "test@example.com", "Test User");
        RefreshToken storedToken = buildRefreshToken(1L, user, false, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(storedToken));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(buildMockJwt("new.token"));

        LoginResponse response = authService.refresh(rawToken);

        assertThat(response.accessToken()).isEqualTo("new.token");
        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refresh - revoked token: throws InvalidTokenException")
    void refresh_revokedToken_throwsInvalidTokenException() {
        String rawToken = "revoked.refresh.token";
        User user = buildUser(1L, "test@example.com", "Test User");
        RefreshToken storedToken = buildRefreshToken(1L, user, true, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(rawToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("thu hồi");
    }

    @Test
    @DisplayName("refresh - expired token: throws InvalidTokenException")
    void refresh_expiredToken_throwsInvalidTokenException() {
        String rawToken = "expired.refresh.token";
        User user = buildUser(1L, "test@example.com", "Test User");
        RefreshToken storedToken = buildRefreshToken(1L, user, false, Instant.now().minusSeconds(3600));

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(rawToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("hết hạn");
    }

    @Test
    @DisplayName("refresh - token not found: throws InvalidTokenException")
    void refresh_tokenNotFound_throwsInvalidTokenException() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("nonexistent.token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ========== logout ==========

    @Test
    @DisplayName("logout - valid token: revokes token")
    void logout_validToken_revokesToken() {
        String rawToken = "valid.refresh.token";
        User user = buildUser(1L, "test@example.com", "Test User");
        RefreshToken storedToken = buildRefreshToken(1L, user, false, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(storedToken));

        authService.logout(rawToken);

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(storedToken);
    }

    @Test
    @DisplayName("logout - invalid token: throws InvalidTokenException")
    void logout_invalidToken_throwsInvalidTokenException() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("invalid.token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ========== getMe ==========

    @Test
    @DisplayName("getMe - found email: returns UserResponse")
    void getMe_foundEmail_returnsUserResponse() {
        User user = buildUser(1L, "test@example.com", "Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.getMe("test@example.com");

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("getMe - not found email: throws ResourceNotFoundException")
    void getMe_notFoundEmail_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== helpers ==========

    private User buildUser(Long id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setPassword("$2a$10$encodedpassword");
        user.setRoles(Collections.emptyList());
        return user;
    }

    private RefreshToken buildRefreshToken(Long id, User user, boolean revoked, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setId(id);
        token.setUser(user);
        token.setToken("hashed-token-value");
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        token.setDeviceInfo("Mozilla/5.0");
        token.setIpAddress("127.0.0.1");
        return token;
    }

    private Jwt buildMockJwt(String tokenValue) {
        return Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .claim("sub", "test@example.com")
                .build();
    }
}
