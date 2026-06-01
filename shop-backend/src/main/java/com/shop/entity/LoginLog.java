package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String ip;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 30)
    private String status;

    @Column(length = 200)
    private String failReason;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}
