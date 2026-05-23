package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.success(categoryService.tree());
    }
}
