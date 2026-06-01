package com.shop.controller.admin;

import com.shop.common.ApiResponse;
import com.shop.common.PageResult;
import com.shop.entity.Inquiry;
import com.shop.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryRepository inquiryRepository;

    @GetMapping
    public ApiResponse<PageResult<Inquiry>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pr = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<Inquiry> result = inquiryRepository.findAll(pr);
        return ApiResponse.success(PageResult.of(result.getContent(), result.getTotalElements(), page, size));
    }

    @PostMapping
    public ApiResponse<Inquiry> create(@RequestBody Inquiry inquiry) {
        if (inquiry.getInquiryNo() == null || inquiry.getInquiryNo().isBlank()) {
            inquiry.setInquiryNo(generateInquiryNo());
        }
        return ApiResponse.success(inquiryRepository.save(inquiry));
    }

    @PutMapping("/{id}")
    public ApiResponse<Inquiry> update(@PathVariable Long id, @RequestBody Inquiry inquiry) {
        Inquiry existing = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("询盘不存在"));
        existing.setCustomer(inquiry.getCustomer());
        existing.setSource(inquiry.getSource());
        existing.setInterest(inquiry.getInterest());
        existing.setSummary(inquiry.getSummary());
        existing.setStatus(inquiry.getStatus());
        return ApiResponse.success(inquiryRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        inquiryRepository.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats")
    public ApiResponse<java.util.Map<String, Object>> stats() {
        List<Inquiry> all = inquiryRepository.findAll();
        long pending = all.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long replied = all.stream().filter(a -> "REPLIED".equals(a.getStatus())).count();
        long converted = all.stream().filter(a -> "CONVERTED".equals(a.getStatus())).count();
        long total = all.size();
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("pending", pending);
        map.put("replied", replied);
        map.put("converted", converted);
        map.put("conversionRate", total > 0 ? Math.round(converted * 10000.0 / total) / 100.0 : 0);
        return ApiResponse.success(map);
    }

    private String generateInquiryNo() {
        long count = inquiryRepository.count();
        return "INQ-" + String.format("%04d", count + 1);
    }
}
