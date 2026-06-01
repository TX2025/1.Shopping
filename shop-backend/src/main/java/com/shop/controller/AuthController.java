package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.dto.request.*;
import com.shop.dto.response.LoginResponse;
import com.shop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiResponse.error(400, "refreshToken不能为空");
        }
        return ApiResponse.success(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        return ApiResponse.success();
    }

    @PostMapping("/send-code")
    public ApiResponse<String> sendCode(@Valid @RequestBody SendCodeRequest request) {
        return ApiResponse.success(authService.sendCode(request));
    }

    @PostMapping("/verify-code")
    public ApiResponse<Boolean> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return ApiResponse.success(authService.verifyCode(request));
    }

    @PostMapping("/login-by-sms")
    public ApiResponse<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        return ApiResponse.success(authService.loginBySms(request));
    }

    @PostMapping("/register-by-sms")
    public ApiResponse<Void> registerBySms(@Valid @RequestBody SmsRegisterRequest request) {
        authService.registerBySms(request);
        return ApiResponse.success();
    }
}
