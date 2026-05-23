package com.shop.service;

import com.shop.entity.Category;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Map<String, Object>> tree();
    List<Category> all();
    Category create(Category category);
    Category update(Long id, Category category);
    void delete(Long id);
}
