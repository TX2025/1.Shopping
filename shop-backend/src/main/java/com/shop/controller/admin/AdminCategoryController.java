package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.Category;
import com.shop.repository.ProductRepository;
import com.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.all());
    }

    @GetMapping("/product-counts")
    public ApiResponse<Map<Long, Long>> productCounts() {
        List<Category> all = categoryService.all();
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Category c : all) {
            counts.put(c.getId(), productRepository.countByCategoryId(c.getId()));
        }
        return ApiResponse.success(counts);
    }

    @PostMapping
    public ApiResponse<Category> create(@RequestBody Category category) {
        return ApiResponse.success(categoryService.create(category));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return ApiResponse.success(categoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success();
    }
}
