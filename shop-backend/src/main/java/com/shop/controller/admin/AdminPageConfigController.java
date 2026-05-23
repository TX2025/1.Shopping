package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.PageConfig;
import com.shop.repository.PageConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPageConfigController {

    private final PageConfigRepository pageConfigRepository;

    @GetMapping("/page-configs")
    public ApiResponse<List<PageConfig>> list() {
        return ApiResponse.success(pageConfigRepository.findAll());
    }

    @PutMapping("/page-configs/{pageType}")
    public ApiResponse<Void> update(@PathVariable String pageType, @RequestBody PageConfig config) {
        pageConfigRepository.findByPageType(pageType).ifPresent(pc -> {
            pc.setConfigJson(config.getConfigJson());
            pageConfigRepository.save(pc);
        });
        return ApiResponse.success();
    }
}
