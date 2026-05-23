package com.shop.service;

import com.shop.dto.request.LoginRequest;
import com.shop.dto.request.RegisterRequest;
import com.shop.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
