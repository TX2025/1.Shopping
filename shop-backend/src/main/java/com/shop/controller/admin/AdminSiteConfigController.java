package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping("/site-configs")
    public ApiResponse<Map<String, String>> list() {
        return ApiResponse.success(siteConfigService.getAllAsMap());
    }

    @PutMapping("/site-configs")
    public ApiResponse<Void> update(@RequestBody Map<String, String> body) {
        siteConfigService.updateAll(body);
        return ApiResponse.success();
    }
}
