package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;            // 活动名称

    @Column(length = 20, nullable = false)
    private String type;            // 满减/折扣/赠品/秒杀

    @Column(length = 200)
    private String discount;        // 优惠描述

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0; // 使用次数

    @Column(name = "start_date")
    private LocalDate startDate;    // 开始日期

    @Column(name = "end_date")
    private LocalDate endDate;      // 结束日期

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "upcoming"; // active/upcoming/ended

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    void prePersist() { this.createTime = LocalDateTime.now(); }
}
