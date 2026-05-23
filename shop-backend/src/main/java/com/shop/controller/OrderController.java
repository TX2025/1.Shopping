package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.dto.request.CreateOrderRequest;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    @PostMapping
    public ApiResponse<Order> create(Authentication auth, @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.create(getUserId(auth), request));
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.userOrders(getUserId(auth), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(Authentication auth, @PathVariable Long id) {
        return ApiResponse.success(orderService.detail(getUserId(auth), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(Authentication auth, @PathVariable Long id) {
        orderService.cancel(getUserId(auth), id);
        return ApiResponse.success();
    }
}
