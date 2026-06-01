package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_method")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;            // 配送方式名称

    @Column(length = 200)
    private String region;          // 配送区域

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;         // 运费

    @Column(precision = 10, scale = 2)
    private BigDecimal freeThreshold; // 免费门槛

    @Column(length = 100)
    private String estimate;        // 预计时效

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "ON";   // ON=启用, OFF=停用

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
