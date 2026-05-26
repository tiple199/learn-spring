package vn.hoidanit.springrestwithai.feature.auth;

import vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest;
import vn.hoidanit.springrestwithai.feature.auth.dto.LoginResponse;
import vn.hoidanit.springrestwithai.feature.auth.dto.RegisterRequest;
import vn.hoidanit.springrestwithai.feature.auth.dto.RegisterResponse;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress);

    RegisterResponse register(RegisterRequest request);

    LoginResponse refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);

    UserResponse getMe(String email);
}
