package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.entity.ShippingMethod;
import com.shop.repository.ShippingMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
public class PublicShippingController {

    private final ShippingMethodRepository shippingMethodRepository;

    @GetMapping
    public ApiResponse<List<ShippingMethod>> list() {
        return ApiResponse.success(shippingMethodRepository.findAll());
    }
}
