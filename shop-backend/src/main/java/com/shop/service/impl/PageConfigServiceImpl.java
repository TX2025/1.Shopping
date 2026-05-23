package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.entity.PageConfig;
import com.shop.repository.PageConfigRepository;
import com.shop.service.PageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PageConfigServiceImpl implements PageConfigService {

    private static final Map<String, String> DEFAULTS = Map.of(
        "HOME", Constants.PAGE_CONFIG_HOME_DEFAULT,
        "PRODUCT_LIST", Constants.PAGE_CONFIG_PRODUCT_LIST_DEFAULT,
        "PRODUCT_DETAIL", Constants.PAGE_CONFIG_PRODUCT_DETAIL_DEFAULT,
        "CART", Constants.PAGE_CONFIG_CART_DEFAULT,
        "PAYMENT", Constants.PAGE_CONFIG_PAYMENT_DEFAULT,
        "THANK_YOU", Constants.PAGE_CONFIG_THANK_YOU_DEFAULT
    );

    private final PageConfigRepository pageConfigRepository;

    @Override
    public PageConfig getConfig(String type) {
        return pageConfigRepository.findByPageType(type.toUpperCase())
            .orElseGet(() -> {
                PageConfig pc = new PageConfig();
                pc.setPageType(type.toUpperCase());
                pc.setConfigJson(DEFAULTS.getOrDefault(type.toUpperCase(), "{}"));
                return pc;
            });
    }

    @Override
    @Transactional
    public void updateConfig(String type, String json) {
        pageConfigRepository.findByPageType(type.toUpperCase()).ifPresentOrElse(
            pc -> { pc.setConfigJson(json); pageConfigRepository.save(pc); },
            () -> pageConfigRepository.save(PageConfig.builder()
                .pageType(type.toUpperCase()).configJson(json).build())
        );
    }

    @Override
    public Map<String, Object> getSchema(String type) {
        return SCHEMAS.getOrDefault(type.toUpperCase(), Map.of());
    }

    private static final Map<String, Map<String, Object>> SCHEMAS = Map.of(
        "HOME", Map.of(
            "hero_enabled", field("boolean", "首页轮播", true),
            "showcase_enabled", field("boolean", "分类展示", true),
            "popular_enabled", field("boolean", "热门推荐", true),
            "trust_enabled", field("boolean", "信任区", true)
        ),
        "PRODUCT_LIST", Map.of(
            "displayMode", field("select:grid,list", "展示模式", "grid"),
            "pageSize", field("number", "每页数量", 20),
            "showSidebar", field("boolean", "显示侧边栏", true),
            "showSalesCount", field("boolean", "显示销量", true),
            "showOriginalPrice", field("boolean", "显示原价", true)
        ),
        "PRODUCT_DETAIL", Map.of(
            "showBreadcrumb", field("boolean", "面包屑导航", true),
            "showRelatedProducts", field("boolean", "相关推荐", true),
            "relatedCount", field("number", "推荐数量", 4),
            "showSalesCount", field("boolean", "显示销量", true),
            "showShareButtons", field("boolean", "分享按钮", true)
        ),
        "CART", Map.of(
            "promotionText", field("string", "促销文案", "满99元免运费"),
            "showCouponInput", field("boolean", "优惠码输入", true),
            "showRelatedProducts", field("boolean", "相关推荐", true),
            "showShippingEstimate", field("boolean", "预估运费", false)
        ),
        "PAYMENT", Map.of(
            "instructionText", field("string", "提示文字", "请在15分钟内完成支付"),
            "showOrderSummary", field("boolean", "订单摘要", true)
        ),
        "THANK_YOU", Map.of(
            "message", field("string", "主标题", "感谢您的购买！"),
            "subMessage", field("string", "副标题", "我们会尽快为您发货"),
            "buttonText", field("string", "按钮文字", "继续购物"),
            "buttonLink", field("string", "按钮链接", "/products"),
            "autoRedirectSeconds", field("number", "自动跳转秒数", 10)
        )
    );

    private static Map<String, Object> field(String type, String label, Object defaultValue) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("label", label);
        m.put("default", defaultValue);
        return m;
    }
}
