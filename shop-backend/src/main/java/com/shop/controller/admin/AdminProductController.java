package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import com.shop.dto.request.BatchDeleteRequest;
import com.shop.dto.request.BatchMoveRequest;
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

    @GetMapping("/by-category")
    public ApiResponse<PageResult<Product>> listByCategory(
            @RequestParam Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(productService.adminListByCategory(categoryId, keyword, status, page, size));
    }

    @PutMapping("/batch-move")
    public ApiResponse<Void> batchMove(@RequestBody BatchMoveRequest body) {
        productService.batchMoveCategory(body.getIds(), body.getTargetCategoryId());
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody BatchDeleteRequest body) {
        productService.batchDelete(body.getIds());
        return ApiResponse.success();
    }
}
