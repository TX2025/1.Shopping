package com.shop.entity;

import com.shop.common.Constants;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 20)
    @Builder.Default
    private String status = Constants.ORDER_STATUS_PENDING;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", length = 500)
    private String receiverAddress;

    @Column(length = 500)
    private String remark;

    @Column(length = 50)
    private String channel;             // 渠道: 微信小程序/APP/PC官网/H5

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;       // 支付方式: 微信支付/支付宝/银行卡

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}
