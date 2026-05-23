package com.shop.service.impl;

import com.shop.dto.request.AddCartRequest;
import com.shop.entity.CartItem;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.repository.CartItemRepository;
import com.shop.repository.ProductRepository;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void add(Long userId, AddCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build());
        }
    }

    @Override
    public List<Map<String, Object>> list(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (CartItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", item.getId());
                m.put("quantity", item.getQuantity());
                m.put("productId", p.getId());
                m.put("productName", p.getName());
                m.put("productPrice", p.getPrice());
                m.put("productImage", p.getCoverImage());
                m.put("stock", p.getStock());
                result.add(m);
            });
        }
        return result;
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(404, "购物车项不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    public void remove(Long userId, Long cartId) {
        CartItem item = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(404, "购物车项不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
