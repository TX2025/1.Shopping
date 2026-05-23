package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.dto.request.AddCartRequest;
import com.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(Authentication auth) {
        return ApiResponse.success(cartService.list(getUserId(auth)));
    }

    @PostMapping
    public ApiResponse<Void> add(Authentication auth, @Valid @RequestBody AddCartRequest request) {
        cartService.add(getUserId(auth), request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(Authentication auth, @PathVariable Long id,
                                    @RequestParam Integer quantity) {
        cartService.updateQuantity(getUserId(auth), id, quantity);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(Authentication auth, @PathVariable Long id) {
        cartService.remove(getUserId(auth), id);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> clear(Authentication auth) {
        cartService.clear(getUserId(auth));
        return ApiResponse.success();
    }
}
