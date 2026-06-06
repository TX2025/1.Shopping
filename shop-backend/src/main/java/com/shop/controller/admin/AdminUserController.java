package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.User;
import com.shop.repository.UserRepository;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.adminList(page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus("ACTIVE");
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("total", total);
        map.put("active", active);
        map.put("disabled", total - active);
        return ApiResponse.success(map);
    }
}
