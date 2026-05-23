package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.Order;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("productCount", productRepository.count());
        result.put("userCount", userRepository.count());

        long totalOrders = orderRepository.count();
        result.put("orderCount", totalOrders);

        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        result.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // Order status distribution
        List<Object[]> statusRows = orderRepository.countByStatus();
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Object[] row : statusRows) {
            statusCounts.put((String) row[0], (Long) row[1]);
        }
        result.put("statusCounts", statusCounts);

        // Daily orders & revenue for last 7 days
        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.minusDays(6).atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        List<Order> recentOrders = orderRepository.findByCreateTimeBetween(startDate, endDate);

        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            List<Order> dayOrders = recentOrders.stream()
                    .filter(o -> !o.getCreateTime().isBefore(dayStart) && o.getCreateTime().isBefore(dayEnd))
                    .collect(Collectors.toList());

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            day.put("count", dayOrders.size());
            day.put("revenue", dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dailyData.add(day);
        }
        result.put("dailyOrders", dailyData);

        // Recent 5 orders
        List<Map<String, Object>> recent = recentOrders.stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(5)
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", o.getId());
                    m.put("orderNo", o.getOrderNo());
                    m.put("totalAmount", o.getTotalAmount());
                    m.put("status", o.getStatus());
                    m.put("createTime", o.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    return m;
                }).collect(Collectors.toList());
        result.put("recentOrders", recent);

        return ApiResponse.success(result);
    }
}
