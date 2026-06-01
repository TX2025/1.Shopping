package com.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SmsRegisterRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;
}
