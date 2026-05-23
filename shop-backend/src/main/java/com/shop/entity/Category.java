package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(length = 500)
    private String icon;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String videos;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}
