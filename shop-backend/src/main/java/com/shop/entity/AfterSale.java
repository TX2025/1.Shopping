package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "after_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfterSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String afterSaleNo;     // 售后单号

    @Column(length = 50)
    private String orderNo;         // 关联订单号

    @Column(length = 100)
    private String customer;        // 客户

    @Column(length = 30, nullable = false)
    private String type;            // RETURN_REFUND=退货退款, EXCHANGE=换货, REFUND_ONLY=仅退款

    @Column(length = 500)
    private String reason;          // 原因

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;      // 金额

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING=待审核, PROCESSING=处理中, COMPLETED=已完成, REJECTED=已拒绝

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
