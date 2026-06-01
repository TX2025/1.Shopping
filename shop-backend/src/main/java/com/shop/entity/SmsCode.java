package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String phone;

    @Column(length = 10)
    private String code;

    @Column(length = 20)
    private String type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}
