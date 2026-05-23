package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.service.PageConfigService;
import com.shop.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;
    private final PageConfigService pageConfigService;

    @GetMapping("/site-config")
    public ApiResponse<?> siteConfig() {
        return ApiResponse.success(siteConfigService.getAllAsMap());
    }

    @GetMapping("/page-config/{type}")
    public ApiResponse<?> pageConfig(@PathVariable String type) {
        return ApiResponse.success(pageConfigService.getConfig(type));
    }
}
