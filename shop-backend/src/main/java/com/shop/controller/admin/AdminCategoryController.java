package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.Category;
import com.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.all());
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
