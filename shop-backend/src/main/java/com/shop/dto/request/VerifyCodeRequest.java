package com.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCodeRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "类型不能为空")
    private String type;
}
