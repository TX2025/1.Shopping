package com.shop.controller;

import com.shop.common.ApiResponse;
import com.shop.entity.PageConfig;
import com.shop.entity.SiteConfig;
import com.shop.repository.PageConfigRepository;
import com.shop.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigRepository siteConfigRepository;
    private final PageConfigRepository pageConfigRepository;

    @GetMapping("/site-config")
    public ApiResponse<Map<String, String>> siteConfig() {
        List<SiteConfig> configs = siteConfigRepository.findAll();
        Map<String, String> map = new LinkedHashMap<>();
        for (SiteConfig sc : configs) {
            map.put(sc.getConfigKey(), sc.getConfigValue());
        }
        return ApiResponse.success(map);
    }

    @GetMapping("/page-config/{type}")
    public ApiResponse<PageConfig> pageConfig(@PathVariable String type) {
        return pageConfigRepository.findByPageType(type.toUpperCase())
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "配置不存在"));
    }
}
