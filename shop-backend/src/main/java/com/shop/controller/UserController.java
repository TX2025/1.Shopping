package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.entity.User;
import com.shop.entity.UserAddress;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    @GetMapping("/profile")
    public ApiResponse<User> profile(Authentication auth) {
        User user = userService.getProfile(getUserId(auth));
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(Authentication auth, @RequestBody User user) {
        User updated = userService.updateProfile(getUserId(auth), user);
        updated.setPassword(null);
        return ApiResponse.success(updated);
    }

    @GetMapping("/addresses")
    public ApiResponse<List<UserAddress>> addresses(Authentication auth) {
        return ApiResponse.success(userService.getAddresses(getUserId(auth)));
    }

    @PostMapping("/addresses")
    public ApiResponse<UserAddress> addAddress(Authentication auth, @RequestBody UserAddress address) {
        return ApiResponse.success(userService.addAddress(getUserId(auth), address));
    }
}
