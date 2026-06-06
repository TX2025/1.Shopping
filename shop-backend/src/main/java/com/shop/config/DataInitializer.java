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
    private final ShippingMethodRepository shippingMethodRepository;
    private final AfterSaleRepository afterSaleRepository;
    private final InquiryRepository inquiryRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initCategories();
        initProducts();
        initSiteConfig();
        initPageConfig();
        initShippingMethods();
        initAfterSales();
        initInquiries();
        initPromotions();
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
            var electronics = categoryRepository.save(Category.builder().name("电子产品").parentId(null).sortOrder(1).icon("Monitor").build());
            categoryRepository.save(Category.builder().name("手机").parentId(electronics.getId()).sortOrder(1).icon("Iphone").build());
            categoryRepository.save(Category.builder().name("电脑").parentId(electronics.getId()).sortOrder(2).icon("Monitor").build());
            categoryRepository.save(Category.builder().name("耳机").parentId(electronics.getId()).sortOrder(3).icon("Headset").build());

            var clothing = categoryRepository.save(Category.builder().name("服装").parentId(null).sortOrder(2).icon("Present").build());
            categoryRepository.save(Category.builder().name("男装").parentId(clothing.getId()).sortOrder(1).icon("User").build());
            categoryRepository.save(Category.builder().name("女装").parentId(clothing.getId()).sortOrder(2).icon("UserFilled").build());

            var home = categoryRepository.save(Category.builder().name("家居生活").parentId(null).sortOrder(3).icon("HomeFilled").build());
            categoryRepository.save(Category.builder().name("家具").parentId(home.getId()).sortOrder(1).icon("Suitcase").build());
            categoryRepository.save(Category.builder().name("厨具").parentId(home.getId()).sortOrder(2).icon("KnifeFork").build());

            log.info("Categories initialized");
        }

        // Upgrade existing categories: set default icons for those without one
        List<Category> missingIcons = categoryRepository.findByIconIsNullOrIcon("");
        if (!missingIcons.isEmpty()) {
            java.util.Map<String, String> defaultIcons = java.util.Map.of(
                "电子产品", "Monitor", "手机", "Iphone", "电脑", "Monitor", "耳机", "Headset",
                "服装", "Present", "男装", "User", "女装", "UserFilled",
                "家居生活", "HomeFilled", "家具", "Suitcase", "厨具", "KnifeFork"
            );
            for (Category c : missingIcons) {
                String icon = defaultIcons.getOrDefault(c.getName(), "FolderOpened");
                c.setIcon(icon);
            }
            categoryRepository.saveAll(missingIcons);
            log.info("Updated {} existing categories with icons", missingIcons.size());
        }
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            String[][] items = {
                {"iPhone 15", "PH-IP15-001", "Apple", "最新款苹果智能手机，A16芯片", "5999.00", "6999.00", "100", "手机", "https://picsum.photos/seed/iphone15/400/400", "hot", "4.6"},
                {"MacBook Pro 14", "CP-MBP14-001", "Apple", "M3芯片，14英寸Liquid Retina显示屏", "12999.00", "14999.00", "50", "电脑", "https://picsum.photos/seed/macbook14/400/400", "recommend", "4.8"},
                {"AirPods Pro 2", "AU-AP2-001", "Apple", "主动降噪无线耳机，H2芯片", "1799.00", "1999.00", "200", "耳机", "https://picsum.photos/seed/airpods2/400/400", "hot", "4.7"},
                {"男士休闲夹克", "MC-JKT-001", "Zara", "春秋季新款，纯棉面料", "299.00", "499.00", "150", "男装", "https://picsum.photos/seed/jacket/400/400", "sale", "4.2"},
                {"女士连衣裙", "WC-DRS-001", "H&M", "夏季新款，碎花设计", "259.00", "399.00", "200", "女装", "https://picsum.photos/seed/dress/400/400", "new", "4.3"},
                {"实木餐桌", "HM-TBL-001", "IKEA", "北欧风格，白橡木材质", "2999.00", "3999.00", "30", "家具", "https://picsum.photos/seed/table/400/400", "recommend", "4.5"},
                {"不粘锅套装", "HM-CKW-001", "Supor", "3件套，适用于所有灶具", "399.00", "599.00", "100", "厨具", "https://picsum.photos/seed/cookware/400/400", "", "4.1"},
                {"iPad Air", "CP-IPA-001", "Apple", "M2芯片，10.9英寸显示屏", "4799.00", "5499.00", "80", "电脑", "https://picsum.photos/seed/ipadair/400/400", "new", "4.9"},
                {"Sony WH-1000XM5", "AU-SNY-001", "Sony", "头戴式降噪耳机", "2499.00", "2999.00", "60", "耳机", "https://picsum.photos/seed/sonyxm5/400/400", "recommend", "4.7"},
                {"Huawei P60", "PH-HWP-001", "Huawei", "华为旗舰手机，XMAGE影像", "4988.00", "5288.00", "120", "手机", "https://picsum.photos/seed/huawei60/400/400", "sale", "4.4"},
            };
            for (String[] item : items) {
                Long catId = categoryRepository.findAllByOrderBySortOrder().stream()
                        .filter(c -> c.getName().equals(item[7]))
                        .findFirst().map(Category::getId).orElse(null);
                productRepository.save(Product.builder()
                        .name(item[0]).sku(item[1]).brand(item[2]).description(item[3])
                        .price(new BigDecimal(item[4])).originalPrice(new BigDecimal(item[5]))
                        .stock(Integer.parseInt(item[6])).categoryId(catId)
                        .coverImage(item[8]).tag(item[9]).rating(Double.parseDouble(item[10]))
                        .status(Constants.PRODUCT_STATUS_ON)
                        .sales((int)(Math.random() * 500))
                        .build());
            }
            log.info("Products initialized");
        }

        // Upgrade existing products: set cover images for those without one
        List<Product> missingCovers = productRepository.findByCoverImageIsNullOrCoverImage("");
        if (!missingCovers.isEmpty()) {
            java.util.Map<String, String> coverMap = java.util.Map.of(
                "iPhone 15", "https://picsum.photos/seed/iphone15/400/400",
                "MacBook Pro 14", "https://picsum.photos/seed/macbook14/400/400",
                "AirPods Pro 2", "https://picsum.photos/seed/airpods2/400/400",
                "男士休闲夹克", "https://picsum.photos/seed/jacket/400/400",
                "女士连衣裙", "https://picsum.photos/seed/dress/400/400",
                "实木餐桌", "https://picsum.photos/seed/table/400/400",
                "不粘锅套装", "https://picsum.photos/seed/cookware/400/400",
                "iPad Air", "https://picsum.photos/seed/ipadair/400/400",
                "Sony WH-1000XM5", "https://picsum.photos/seed/sonyxm5/400/400",
                "Huawei P60", "https://picsum.photos/seed/huawei60/400/400"
            );
            for (Product p : missingCovers) {
                String cover = coverMap.getOrDefault(p.getName(), "https://picsum.photos/seed/" + p.getId() + "/400/400");
                p.setCoverImage(cover);
            }
            productRepository.saveAll(missingCovers);
            log.info("Updated {} existing products with cover images", missingCovers.size());
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
        }
        // Always ensure new keys exist (incremental update for older installations)
        Constants.SITE_CONFIG_DEFAULTS.forEach((key, defaultValue) -> {
            if (siteConfigRepository.findByConfigKey(key).isEmpty()) {
                siteConfigRepository.save(SiteConfig.builder().configKey(key).configValue(defaultValue).description("").build());
                log.info("Added site config: {}", key);
            }
        });
        log.info("Site configs initialized");
    }

    private void initPageConfig() {
        java.util.Map<String, String> defaults = java.util.Map.of(
            "HOME", Constants.PAGE_CONFIG_HOME_DEFAULT,
            "PRODUCT_LIST", Constants.PAGE_CONFIG_PRODUCT_LIST_DEFAULT,
            "PRODUCT_DETAIL", Constants.PAGE_CONFIG_PRODUCT_DETAIL_DEFAULT,
            "CART", Constants.PAGE_CONFIG_CART_DEFAULT,
            "PAYMENT", Constants.PAGE_CONFIG_PAYMENT_DEFAULT,
            "THANK_YOU", Constants.PAGE_CONFIG_THANK_YOU_DEFAULT
        );
        defaults.forEach((type, json) -> {
            if (pageConfigRepository.findByPageType(type).isEmpty()) {
                pageConfigRepository.save(PageConfig.builder().pageType(type).configJson(json).build());
            }
        });
        log.info("Page configs initialized (existing preserved)");
    }

    private SiteConfig config(String key, String value, String desc) {
        return SiteConfig.builder().configKey(key).configValue(value).description(desc).build();
    }

    private PageConfig pageConfig(String type, String json) {
        return PageConfig.builder().pageType(type).configJson(json).build();
    }

    private void initShippingMethods() {
        if (shippingMethodRepository.count() == 0) {
            shippingMethodRepository.save(ShippingMethod.builder()
                    .name("标准快递").region("全国").fee(new java.math.BigDecimal("8.00"))
                    .freeThreshold(new java.math.BigDecimal("99.00")).estimate("3-5个工作日").status("ON").build());
            shippingMethodRepository.save(ShippingMethod.builder()
                    .name("加急配送").region("一二线城市").fee(new java.math.BigDecimal("18.00"))
                    .freeThreshold(new java.math.BigDecimal("299.00")).estimate("1-2个工作日").status("ON").build());
            shippingMethodRepository.save(ShippingMethod.builder()
                    .name("国际物流").region("海外").fee(new java.math.BigDecimal("50.00"))
                    .freeThreshold(null).estimate("7-15个工作日").status("ON").build());
            shippingMethodRepository.save(ShippingMethod.builder()
                    .name("到店自提").region("指定门店").fee(java.math.BigDecimal.ZERO)
                    .freeThreshold(null).estimate("当日可提").status("OFF").build());
            log.info("Shipping methods initialized");
        }
    }

    private void initAfterSales() {
        if (afterSaleRepository.count() == 0) {
            afterSaleRepository.save(AfterSale.builder()
                    .afterSaleNo("#AS-2024-001").orderNo("#ORD-2024-001").customer("张三")
                    .type("RETURN_REFUND").reason("质量问题")
                    .amount(new java.math.BigDecimal("299.00")).status("PENDING").build());
            afterSaleRepository.save(AfterSale.builder()
                    .afterSaleNo("#AS-2024-002").orderNo("#ORD-2024-003").customer("王五")
                    .type("EXCHANGE").reason("尺寸不合")
                    .amount(java.math.BigDecimal.ZERO).status("PROCESSING").build());
            afterSaleRepository.save(AfterSale.builder()
                    .afterSaleNo("#AS-2024-003").orderNo("#ORD-2024-005").customer("孙七")
                    .type("REFUND_ONLY").reason("不想要了")
                    .amount(new java.math.BigDecimal("2399.00")).status("REJECTED").build());
            log.info("After-sales initialized");
        }
    }

    private void initInquiries() {
        if (inquiryRepository.count() == 0) {
            inquiryRepository.save(Inquiry.builder()
                    .inquiryNo("#INQ-001").customer("John Smith").source("官网")
                    .interest("智能手表 Pro").summary("批量价格咨询，MOQ 500pcs...")
                    .status("PENDING").build());
            inquiryRepository.save(Inquiry.builder()
                    .inquiryNo("#INQ-002").customer("Maria Garcia").source("WhatsApp")
                    .interest("运动相机 4K").summary("产品规格及定制LOGO需求...")
                    .status("PENDING").build());
            inquiryRepository.save(Inquiry.builder()
                    .inquiryNo("#INQ-003").customer("陈先生").source("官网")
                    .interest("机械键盘 87键").summary("能否定制键帽配色？最小起订量多少？")
                    .status("REPLIED").build());
            log.info("Inquiries initialized");
        }
    }

    private void initPromotions() {
        if (promotionRepository.count() == 0) {
            java.time.LocalDate now = java.time.LocalDate.now();
            promotionRepository.save(Promotion.builder().name("新年特惠").type("满减").discount("满200减30")
                    .usageCount(1256).startDate(now.minusDays(10)).endDate(now.plusDays(20)).status("active").build());
            promotionRepository.save(Promotion.builder().name("新用户专享").type("折扣").discount("首单8折")
                    .usageCount(2100).startDate(now.minusDays(30)).endDate(now.plusDays(60)).status("active").build());
            promotionRepository.save(Promotion.builder().name("双12大促").type("满减").discount("满500减80")
                    .usageCount(3421).startDate(now.minusDays(60)).endDate(now.minusDays(40)).status("ended").build());
            promotionRepository.save(Promotion.builder().name("秒杀专场").type("秒杀").discount("限量5折起")
                    .startDate(now.plusDays(5)).endDate(now.plusDays(7)).status("upcoming").build());
            log.info("Promotions initialized");
        }
    }
}
