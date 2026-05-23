package com.shop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "地址不能为空")
    private Long addressId;

    private String remark;
}
