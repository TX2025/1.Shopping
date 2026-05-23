package com.shop.service;

import com.shop.dto.request.AddCartRequest;
import java.util.List;
import java.util.Map;

public interface CartService {
    void add(Long userId, AddCartRequest request);
    List<Map<String, Object>> list(Long userId);
    void updateQuantity(Long userId, Long cartId, Integer quantity);
    void remove(Long userId, Long cartId);
    void clear(Long userId);
}
