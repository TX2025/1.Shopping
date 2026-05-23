package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResult<Product>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(productService.adminList(status, page, size));
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody Product product) {
        return ApiResponse.success(productService.create(product));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ApiResponse.success(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateStatus(id, status);
        return ApiResponse.success();
    }
}
