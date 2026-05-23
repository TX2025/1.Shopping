package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.dto.request.LoginRequest;
import com.shop.dto.request.RegisterRequest;
import com.shop.dto.response.LoginResponse;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.repository.UserRepository;
import com.shop.security.JwtUtils;
import com.shop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Constants.ROLE_USER)
                .status(Constants.USER_STATUS_ACTIVE)
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(400, "账号已被禁用");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
