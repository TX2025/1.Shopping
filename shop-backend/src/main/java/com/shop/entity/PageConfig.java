package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_type", nullable = false, unique = true, length = 50)
    private String pageType;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
