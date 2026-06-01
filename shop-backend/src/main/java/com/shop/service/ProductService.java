package com.shop.service;

import com.shop.common.PageResult;
import com.shop.entity.Product;

public interface ProductService {
    PageResult<Product> list(Long categoryId, String keyword, String sort, int page, int size);
    Product detail(Long id);
    PageResult<Product> adminList(String status, int page, int size);
    Product create(Product product);
    Product update(Long id, Product product);
    void delete(Long id);
    void updateStatus(Long id, String status);
    PageResult<Product> adminListByCategory(Long categoryId, String keyword, String status, int page, int size);
    void batchMoveCategory(java.util.List<Long> ids, Long targetCategoryId);
    void batchDelete(java.util.List<Long> ids);
}
