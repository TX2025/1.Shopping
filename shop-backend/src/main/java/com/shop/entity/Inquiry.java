package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String inquiryNo;       // 询盘编号

    @Column(length = 100)
    private String customer;        // 客户

    @Column(length = 50)
    private String source;          // 来源 (官网/WhatsApp/邮件/电话)

    @Column(length = 200)
    private String interest;        // 产品兴趣

    @Column(length = 1000)
    private String summary;         // 内容摘要

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING=待回复, REPLIED=已回复, CONVERTED=已转化

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
