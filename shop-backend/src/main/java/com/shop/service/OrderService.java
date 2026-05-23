package com.shop.service;

import com.shop.common.PageResult;
import com.shop.dto.request.CreateOrderRequest;
import com.shop.entity.Order;
import java.util.Map;

public interface OrderService {
    Order create(Long userId, CreateOrderRequest request);
    Map<String, Object> detail(Long userId, Long orderId);
    PageResult<Map<String, Object>> userOrders(Long userId, int page, int size);
    void cancel(Long userId, Long orderId);
    PageResult<Map<String, Object>> adminOrders(String status, int page, int size);
    void updateStatus(Long orderId, String status);
}
