package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.entity.Promotion;
import com.shop.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionRepository promotionRepository;

    @GetMapping
    public ApiResponse<List<Promotion>> list(@RequestParam(required = false) String status) {
        List<Promotion> all = status != null && !status.isBlank()
                ? promotionRepository.findByStatus(status)
                : promotionRepository.findAll();
        return ApiResponse.success(all);
    }

    @PostMapping
    public ApiResponse<Promotion> create(@RequestBody Promotion p) { return ApiResponse.success(promotionRepository.save(p)); }

    @PutMapping("/{id}")
    public ApiResponse<Promotion> update(@PathVariable Long id, @RequestBody Promotion p) {
        Promotion e = promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("活动不存在"));
        e.setName(p.getName()); e.setType(p.getType()); e.setDiscount(p.getDiscount());
        e.setUsageCount(p.getUsageCount()); e.setStartDate(p.getStartDate()); e.setEndDate(p.getEndDate()); e.setStatus(p.getStatus());
        return ApiResponse.success(promotionRepository.save(e));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { promotionRepository.deleteById(id); return ApiResponse.success(); }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        List<Promotion> all = promotionRepository.findAll();
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("total", all.size());
        m.put("active", all.stream().filter(p->"active".equals(p.getStatus())).count());
        m.put("upcoming", all.stream().filter(p->"upcoming".equals(p.getStatus())).count());
        m.put("ended", all.stream().filter(p->"ended".equals(p.getStatus())).count());
        m.put("totalUsage", all.stream().mapToInt(Promotion::getUsageCount).sum());
        return ApiResponse.success(m);
    }
}
