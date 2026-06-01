package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.AfterSale;
import com.shop.repository.AfterSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/aftersales")
@RequiredArgsConstructor
public class AdminAfterSaleController {

    private final AfterSaleRepository afterSaleRepository;

    @GetMapping
    public ApiResponse<PageResult<AfterSale>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<AfterSale> result = afterSaleRepository.findAll(pr);
        return ApiResponse.success(PageResult.of(result.getContent(), result.getTotalElements(), page, size));
    }

    @PostMapping
    public ApiResponse<AfterSale> create(@RequestBody AfterSale afterSale) {
        if (afterSale.getAfterSaleNo() == null || afterSale.getAfterSaleNo().isBlank()) {
            afterSale.setAfterSaleNo(generateAfterSaleNo());
        }
        return ApiResponse.success(afterSaleRepository.save(afterSale));
    }

    @PutMapping("/{id}")
    public ApiResponse<AfterSale> update(@PathVariable Long id, @RequestBody AfterSale afterSale) {
        AfterSale existing = afterSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("售后单不存在"));
        existing.setType(afterSale.getType());
        existing.setReason(afterSale.getReason());
        existing.setCustomer(afterSale.getCustomer());
        existing.setAmount(afterSale.getAmount());
        existing.setStatus(afterSale.getStatus());
        existing.setOrderNo(afterSale.getOrderNo());
        return ApiResponse.success(afterSaleRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        afterSaleRepository.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats")
    public ApiResponse<java.util.Map<String, Long>> stats() {
        List<AfterSale> all = afterSaleRepository.findAll();
        long pending = all.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long processing = all.stream().filter(a -> "PROCESSING".equals(a.getStatus())).count();
        long completed = all.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long rejected = all.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
        java.util.Map<String, Long> map = new java.util.LinkedHashMap<>();
        map.put("pending", pending);
        map.put("processing", processing);
        map.put("completed", completed);
        map.put("rejected", rejected);
        return ApiResponse.success(map);
    }

    private String generateAfterSaleNo() {
        long count = afterSaleRepository.count();
        return "AS-" + String.format("%04d", count + 1);
    }
}
