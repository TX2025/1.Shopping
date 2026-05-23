package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.common.PageResult;
import com.shop.dto.request.CreateOrderRequest;
import com.shop.entity.*;
import com.shop.exception.BusinessException;
import com.shop.repository.*;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public Order create(Long userId, CreateOrderRequest request) {
        UserAddress address = userAddressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BusinessException(404, "地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权使用该地址");
        }
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(400, "购物车为空");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            Product p = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new BusinessException(404, "商品不存在"));
            if (ci.getQuantity() > p.getStock()) {
                throw new BusinessException(400, "商品 " + p.getName() + " 库存不足");
            }
            BigDecimal itemTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(itemTotal);
            orderItems.add(OrderItem.builder()
                    .productId(p.getId()).productName(p.getName())
                    .productImage(p.getCoverImage()).productPrice(p.getPrice())
                    .quantity(ci.getQuantity()).build());
            p.setStock(p.getStock() - ci.getQuantity());
            p.setSales(p.getSales() + ci.getQuantity());
            productRepository.save(p);
        }

        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", new Random().nextInt(1000000));

        Order order = Order.builder()
                .orderNo(orderNo).userId(userId).totalAmount(total)
                .status(Constants.ORDER_STATUS_PENDING)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + " " + address.getDetail())
                .remark(request.getRemark()).build();
        order = orderRepository.save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemRepository.save(oi);
        }

        cartItemRepository.deleteByUserId(userId);
        return order;
    }

    @Override
    public Map<String, Object> detail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("order", order);
        m.put("items", orderItemRepository.findByOrderId(orderId));
        return m;
    }

    @Override
    public PageResult<Map<String, Object>> userOrders(Long userId, int page, int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Order> result = orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pr);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : result.getContent()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("order", o);
            m.put("items", orderItemRepository.findByOrderId(o.getId()));
            list.add(m);
        }
        return PageResult.of(list, result.getTotalElements(), page, size);
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!Constants.ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(400, "只能取消待支付订单");
        }
        order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public PageResult<Map<String, Object>> adminOrders(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Order> result = orderRepository.findAll(pr);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : result.getContent()) {
            if (status != null && !status.isBlank() && !status.equals(o.getStatus())) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("order", o);
            m.put("items", orderItemRepository.findByOrderId(o.getId()));
            list.add(m);
        }
        return PageResult.of(list, result.getTotalElements(), page, size);
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        order.setStatus(status);
        if (Constants.ORDER_STATUS_PAID.equals(status)) {
            order.setPayTime(LocalDateTime.now());
        }
        orderRepository.save(order);
    }
}
