package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.ShippingMethod;
import com.shop.repository.ShippingMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
public class AdminShippingController {

    private final ShippingMethodRepository shippingMethodRepository;

    @GetMapping
    public ApiResponse<List<ShippingMethod>> list() {
        return ApiResponse.success(shippingMethodRepository.findAll());
    }

    @PostMapping
    public ApiResponse<ShippingMethod> create(@RequestBody ShippingMethod method) {
        return ApiResponse.success(shippingMethodRepository.save(method));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShippingMethod> update(@PathVariable Long id, @RequestBody ShippingMethod method) {
        ShippingMethod existing = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配送方式不存在"));
        existing.setName(method.getName());
        existing.setRegion(method.getRegion());
        existing.setFee(method.getFee());
        existing.setFreeThreshold(method.getFreeThreshold());
        existing.setEstimate(method.getEstimate());
        existing.setStatus(method.getStatus());
        return ApiResponse.success(shippingMethodRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        shippingMethodRepository.deleteById(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        ShippingMethod existing = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配送方式不存在"));
        existing.setStatus(status);
        shippingMethodRepository.save(existing);
        return ApiResponse.success();
    }
}
