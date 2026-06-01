package com.shop.repository;

import com.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.status = 'ON' " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findProducts(@Param("categoryId") Long categoryId,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId " +
           "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findByCategoryIdAndKeyword(@Param("categoryId") Long categoryId,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    long countByCategoryId(Long categoryId);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.status = :status " +
           "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findByCategoryIdAndStatusAndKeyword(@Param("categoryId") Long categoryId,
                                                       @Param("status") String status,
                                                       @Param("keyword") String keyword,
                                                       Pageable pageable);

    List<Product> findByCoverImageIsNullOrCoverImage(String coverImage);
}
