package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.common.PageResult;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public PageResult<Product> list(Long categoryId, String keyword, String sort, int page, int size) {
        Sort s = switch (sort != null ? sort : "newest") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "sales" -> Sort.by("sales").descending();
            default -> Sort.by("createTime").descending();
        };
        PageRequest pr = PageRequest.of(page - 1, size, s);
        Page<Product> result = productRepository.findProducts(categoryId,
                (keyword != null && !keyword.isBlank()) ? keyword : null, pr);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public Product detail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (Constants.PRODUCT_STATUS_OFF.equals(product.getStatus())) {
            throw new BusinessException(404, "商品已下架");
        }
        return product;
    }

    @Override
    public PageResult<Product> adminList(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Product> result = (status != null && !status.isBlank())
                ? productRepository.findByStatus(status, pr)
                : productRepository.findAll(pr);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setOriginalPrice(product.getOriginalPrice());
        existing.setStock(product.getStock());
        existing.setCategoryId(product.getCategoryId());
        existing.setCoverImage(product.getCoverImage());
        existing.setImages(product.getImages());
        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
    }
}
