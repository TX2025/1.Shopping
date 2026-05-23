package com.shop.repository;

import com.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.status = 'ON' " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findProducts(@Param("categoryId") Long categoryId,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);
}
