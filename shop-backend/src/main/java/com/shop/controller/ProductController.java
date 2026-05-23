package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ApiResponse<PageResult<Product>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(productService.list(categoryId, keyword, sort, page, size));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Product> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.detail(id));
    }
}
