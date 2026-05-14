package vn.hoidanit.springrestwithai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.springrestwithai.helper.ApiResponse;
import vn.hoidanit.springrestwithai.model.dto.request.LoginRequest;
import vn.hoidanit.springrestwithai.model.dto.response.LoginResponse;
import vn.hoidanit.springrestwithai.service.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpiration;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpiration = jwtExpiration;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        String accessToken = jwtService.generateToken(authentication);
        LoginResponse loginResponse = new LoginResponse(accessToken, "Bearer", jwtExpiration);
        return ResponseEntity.ok(ApiResponse.success("Dang nhap thanh cong", loginResponse));
    }
}
