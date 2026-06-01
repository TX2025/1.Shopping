package com.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long userId;
    private String username;
    private String role;
}
