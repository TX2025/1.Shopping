package com.shop.repository;

import com.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal sumTotalRevenue();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();

    @Query("SELECT o FROM Order o WHERE o.createTime >= :start AND o.createTime < :end ORDER BY o.createTime ASC")
    List<Order> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}
