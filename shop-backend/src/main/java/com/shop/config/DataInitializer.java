package com.shop.config;

import com.shop.common.Constants;
import com.shop.entity.*;
import com.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SiteConfigRepository siteConfigRepository;
    private final PageConfigRepository pageConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initCategories();
        initProducts();
        initSiteConfig();
        initPageConfig();
    }

    private void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@shop.com")
                    .role(Constants.ROLE_ADMIN)
                    .status(Constants.USER_STATUS_ACTIVE)
                    .build());
            log.info("Admin user created: admin/admin123");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            var electronics = categoryRepository.save(Category.builder().name("电子产品").parentId(null).sortOrder(1).build());
            categoryRepository.save(Category.builder().name("手机").parentId(electronics.getId()).sortOrder(1).build());
            categoryRepository.save(Category.builder().name("电脑").parentId(electronics.getId()).sortOrder(2).build());
            categoryRepository.save(Category.builder().name("耳机").parentId(electronics.getId()).sortOrder(3).build());

            var clothing = categoryRepository.save(Category.builder().name("服装").parentId(null).sortOrder(2).build());
            categoryRepository.save(Category.builder().name("男装").parentId(clothing.getId()).sortOrder(1).build());
            categoryRepository.save(Category.builder().name("女装").parentId(clothing.getId()).sortOrder(2).build());

            var home = categoryRepository.save(Category.builder().name("家居生活").parentId(null).sortOrder(3).build());
            categoryRepository.save(Category.builder().name("家具").parentId(home.getId()).sortOrder(1).build());
            categoryRepository.save(Category.builder().name("厨具").parentId(home.getId()).sortOrder(2).build());

            log.info("Categories initialized");
        }
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            String[][] items = {
                {"iPhone 15", "最新款苹果智能手机，A16芯片", "5999.00", "6999.00", "100", "手机"},
                {"MacBook Pro 14", "M3芯片，14英寸Liquid Retina显示屏", "12999.00", "14999.00", "50", "电脑"},
                {"AirPods Pro 2", "主动降噪无线耳机，H2芯片", "1799.00", "1999.00", "200", "耳机"},
                {"男士休闲夹克", "春秋季新款，纯棉面料", "299.00", "499.00", "150", "男装"},
                {"女士连衣裙", "夏季新款，碎花设计", "259.00", "399.00", "200", "女装"},
                {"实木餐桌", "北欧风格，白橡木材质", "2999.00", "3999.00", "30", "家具"},
                {"不粘锅套装", "3件套，适用于所有灶具", "399.00", "599.00", "100", "厨具"},
                {"iPad Air", "M2芯片，10.9英寸显示屏", "4799.00", "5499.00", "80", "电脑"},
                {"Sony WH-1000XM5", "头戴式降噪耳机", "2499.00", "2999.00", "60", "耳机"},
                {"Huawei P60", "华为旗舰手机，XMAGE影像", "4988.00", "5288.00", "120", "手机"},
            };
            for (String[] item : items) {
                Long catId = categoryRepository.findAllByOrderBySortOrder().stream()
                        .filter(c -> c.getName().equals(item[5]))
                        .findFirst().map(Category::getId).orElse(null);
                productRepository.save(Product.builder()
                        .name(item[0]).description(item[1])
                        .price(new BigDecimal(item[2])).originalPrice(new BigDecimal(item[3]))
                        .stock(Integer.parseInt(item[4])).categoryId(catId)
                        .status(Constants.PRODUCT_STATUS_ON)
                        .sales((int)(Math.random() * 500))
                        .build());
            }
            log.info("Products initialized");
        }
    }

    private void initSiteConfig() {
        if (siteConfigRepository.count() == 0) {
            List<SiteConfig> configs = List.of(
                config("site_name", "MyShop", "网站名称"),
                config("contact_phone", "400-123-4567", "联系电话"),
                config("contact_email", "support@shop.com", "联系邮箱"),
                config("contact_hours", "周一至周五 9:00-18:00", "客服时间"),
                config("site_logo", "", "Logo URL"),
                config("marketing_popup_enabled", "false", "促销弹窗开关"),
                config("marketing_popup_content", "{\"title\":\"新用户优惠\",\"content\":\"注册即享首单9折优惠！\"}", "弹窗内容")
            );
            siteConfigRepository.saveAll(configs);
            log.info("Site configs initialized");
        }
    }

    private void initPageConfig() {
        if (pageConfigRepository.count() == 0) {
            List<PageConfig> configs = List.of(
                pageConfig("HOME", "{\"banners\":[],\"sections\":[{\"type\":\"category_showcase\",\"title\":\"热门分类\",\"categoryIds\":[]},{\"type\":\"product_grid\",\"title\":\"推荐商品\",\"displayCount\":8,\"sortBy\":\"sales\"}]}"),
                pageConfig("PRODUCT_LIST", "{\"displayMode\":\"grid\",\"pageSize\":20,\"showFilters\":[\"category\",\"price\",\"keyword\"],\"defaultSort\":\"newest\",\"sidebarPosition\":\"left\"}"),
                pageConfig("PRODUCT_DETAIL", "{\"showRelatedProducts\":true,\"relatedCount\":4,\"imageDisplayMode\":\"thumbnail\",\"showSalesCount\":true}"),
                pageConfig("CART", "{\"showRelatedProducts\":true,\"promotionText\":\"满99元免运费\",\"showQuantityDiscount\":false}"),
                pageConfig("PAYMENT", "{\"paymentMethods\":[\"alipay\",\"wechat\"],\"instructionText\":\"请在15分钟内完成支付\",\"showOrderSummary\":true}"),
                pageConfig("THANK_YOU", "{\"message\":\"感谢您的购买！\",\"subMessage\":\"我们会尽快为您发货\",\"buttonText\":\"继续购物\",\"buttonLink\":\"/products\",\"autoRedirectSeconds\":10}")
            );
            pageConfigRepository.saveAll(configs);
            log.info("Page configs initialized");
        }
    }

    private SiteConfig config(String key, String value, String desc) {
        return SiteConfig.builder().configKey(key).configValue(value).description(desc).build();
    }

    private PageConfig pageConfig(String type, String json) {
        return PageConfig.builder().pageType(type).configJson(json).build();
    }
}
