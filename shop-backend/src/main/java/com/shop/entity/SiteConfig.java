package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(length = 500)
    private String description;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
