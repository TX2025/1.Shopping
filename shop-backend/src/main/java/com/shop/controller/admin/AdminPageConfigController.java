package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.PageConfig;
import com.shop.service.PageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPageConfigController {

    private final PageConfigService pageConfigService;

    @GetMapping("/page-configs")
    public ApiResponse<Map<String, Object>> list() {
        var map = new java.util.LinkedHashMap<String, Object>();
        for (String type : new String[]{"HOME", "PRODUCT_LIST", "PRODUCT_DETAIL", "CART", "PAYMENT", "THANK_YOU", "FOOTER"}) {
            map.put(type, pageConfigService.getConfig(type));
        }
        return ApiResponse.success(map);
    }

    @PutMapping("/page-configs/{pageType}")
    public ApiResponse<Void> update(@PathVariable String pageType, @RequestBody PageConfig config) {
        pageConfigService.updateConfig(pageType, config.getConfigJson());
        return ApiResponse.success();
    }

    @GetMapping("/page-configs/{pageType}/schema")
    public ApiResponse<Map<String, Object>> schema(@PathVariable String pageType) {
        return ApiResponse.success(pageConfigService.getSchema(pageType));
    }
}
