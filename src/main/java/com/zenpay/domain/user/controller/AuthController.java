package com.zenpay.domain.user.controller;

import com.zenpay.domain.user.dto.LoginRequest;
import com.zenpay.domain.user.dto.SignupRequest;
import com.zenpay.domain.user.dto.TokenResponse;
import com.zenpay.domain.user.service.AuthService;
import com.zenpay.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.ok("회원가입이 완료되었습니다.", null);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        return ApiResponse.ok(authService.reissue(refreshToken));
    }

    @DeleteMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }
}