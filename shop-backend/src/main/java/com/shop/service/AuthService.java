package com.shop.service;

import com.shop.dto.request.*;
import com.shop.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(String refreshToken);
    void logout(String refreshToken);
    String sendCode(SendCodeRequest request);
    boolean verifyCode(VerifyCodeRequest request);
    LoginResponse loginBySms(SmsLoginRequest request);
    void registerBySms(SmsRegisterRequest request);
}
