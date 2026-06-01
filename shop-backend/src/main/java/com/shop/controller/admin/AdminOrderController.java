package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.repository.OrderRepository;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.adminOrders(status, page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        List<Object[]> counts = orderRepository.countByStatus();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("PENDING", 0L);
        map.put("PAID", 0L);
        map.put("SHIPPED", 0L);
        map.put("CANCELLED", 0L);
        for (Object[] row : counts) {
            map.put((String) row[0], (Long) row[1]);
        }
        return ApiResponse.success(map);
    }
}
