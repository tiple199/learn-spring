package vn.hoidanit.springrestwithai.feature.auth;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest;
import vn.hoidanit.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataFactory testDataFactory;

    private static final String SEED_EMAIL = "testuser@example.com";
    private static final String SEED_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Seed RBAC permissions cho /me và /logout (không nằm trong whitelist)
        testDataFactory.seedPermissions("AUTH", "/api/v1/auth/me", "GET");
        testDataFactory.seedPermissions("AUTH", "/api/v1/auth/logout", "POST");

        User user = new User();
        user.setEmail(SEED_EMAIL);
        user.setName("Test User");
        user.setPassword(passwordEncoder.encode(SEED_PASSWORD));
        user.setAge(25);
        user.setGender(GenderEnum.MALE);
        user.setAddress("Ho Chi Minh City");
        userRepository.save(user);
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        testDataFactory.cleanup();
    }

    // ========== POST /api/v1/auth/register ==========

    @Test
    @DisplayName("POST /auth/register - 201: creates user and returns response")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "New User", "newuser@example.com", "password123", 30, GenderEnum.FEMALE, "Ha Noi");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.name", is("New User")))
                .andExpect(jsonPath("$.data.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.data.age", is(30)))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/register - 400: blank fields return validation error")
    void register_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /auth/register - 409: duplicate email")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Duplicate", SEED_EMAIL, "password123", 25, GenderEnum.MALE, "HCM");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode", is(409)));
    }

    // ========== POST /api/v1/auth/login ==========

    @Test
    @DisplayName("POST /auth/login - 200: returns tokens and Set-Cookie header")
    void login_validCredentials_returns200WithTokens() throws Exception {
        LoginRequest request = new LoginRequest(SEED_EMAIL, SEED_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(result -> {
                    String setCookie = result.getResponse().getHeader("Set-Cookie");
                    assert setCookie != null && setCookie.contains("refresh_token");
                });
    }

    @Test
    @DisplayName("POST /auth/login - 401: wrong password")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest(SEED_EMAIL, "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)));
    }

    @Test
    @DisplayName("POST /auth/login - 401: non-existent email")
    void login_nonExistentEmail_returns401() throws Exception {
        LoginRequest request = new LoginRequest("nobody@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)));
    }

    @Test
    @DisplayName("POST /auth/login - 400: missing fields")
    void login_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    // ========== POST /api/v1/auth/refresh ==========

    @Test
    @DisplayName("POST /auth/refresh - 200: refreshes tokens via cookie")
    void refresh_validCookieToken_returns200() throws Exception {
        // First login to get a refresh token cookie
        LoginRequest loginRequest = new LoginRequest(SEED_EMAIL, SEED_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String refreshToken = extractCookieValue(setCookie, "refresh_token");

        // Use the refresh token cookie
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()));
    }

    @Test
    @DisplayName("POST /auth/refresh - 401: no token provided")
    void refresh_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)));
    }

    // ========== POST /api/v1/auth/logout ==========

    @Test
    @DisplayName("POST /auth/logout - 200: clears cookie")
    void logout_withValidToken_returns200() throws Exception {
        // First login to get a refresh token cookie
        LoginRequest loginRequest = new LoginRequest(SEED_EMAIL, SEED_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String refreshToken = extractCookieValue(setCookie, "refresh_token");

        // Logout dùng mock JWT (có permission) + refresh cookie
        // Real JWT từ login không có ROLE_TEST_ROLE nên sẽ bị 403
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(testDataFactory.jwtWithPermission(SEED_EMAIL))
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(result -> {
                    String cookie = result.getResponse().getHeader("Set-Cookie");
                    assert cookie != null && cookie.contains("Max-Age=0");
                });
    }

    @Test
    @DisplayName("POST /auth/logout - 200: graceful without cookie")
    void logout_withoutCookie_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));
    }

    @Test
    @DisplayName("POST /auth/logout - 403: no permission returns forbidden")
    void logout_noPermission_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== GET /api/v1/auth/me ==========

    @Test
    @DisplayName("GET /auth/me - 200: returns user data with JWT")
    void getMe_withValidJwt_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(testDataFactory.jwtWithPermission(SEED_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.data.email", is(SEED_EMAIL)))
                .andExpect(jsonPath("$.data.name", is("Test User")));
    }

    @Test
    @DisplayName("GET /auth/me - 401: no token")
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/me - 403: no permission returns forbidden")
    void getMe_noPermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }

    // ========== helpers ==========

    private String extractCookieValue(String setCookieHeader, String cookieName) {
        if (setCookieHeader == null) {
            return null;
        }
        for (String part : setCookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(cookieName + "=")) {
                return trimmed.substring(cookieName.length() + 1);
            }
        }
        return null;
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }
}
