package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.SiteConfig;
import com.shop.repository.SiteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSiteConfigController {

    private final SiteConfigRepository siteConfigRepository;

    @GetMapping("/site-configs")
    public ApiResponse<List<SiteConfig>> list() {
        return ApiResponse.success(siteConfigRepository.findAll());
    }

    @PutMapping("/site-configs")
    public ApiResponse<Void> update(@RequestBody Map<String, List<Map<String, String>>> body) {
        List<Map<String, String>> configs = body.get("configs");
        if (configs != null) {
            for (Map<String, String> c : configs) {
                siteConfigRepository.findByConfigKey(c.get("key")).ifPresent(sc -> {
                    sc.setConfigValue(c.get("value"));
                    siteConfigRepository.save(sc);
                });
            }
        }
        return ApiResponse.success();
    }
}
