package com.shop.common;

public class Constants {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String PRODUCT_STATUS_ON = "ON";
    public static final String PRODUCT_STATUS_OFF = "OFF";

    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_DISABLED = "DISABLED";

    // ========== Site Config Keys ==========
    public static final String SITE_CONFIG_PROMO_BAR_ENABLED = "promo_bar_enabled";
    public static final String SITE_CONFIG_PROMO_BAR_MESSAGES = "promo_bar_messages";
    public static final String SITE_CONFIG_HEADER_PHONE = "header_phone";
    public static final String SITE_CONFIG_HERO_ENABLED = "hero_enabled";
    public static final String SITE_CONFIG_HERO_SLIDES = "hero_slides";
    public static final String SITE_CONFIG_HERO_INTERVAL = "hero_interval";
    public static final String SITE_CONFIG_POPULAR_TITLE = "popular_title";
    public static final String SITE_CONFIG_POPULAR_COUNT = "popular_count";
    public static final String SITE_CONFIG_POPULAR_SORT = "popular_sort";
    public static final String SITE_CONFIG_TRUST_ENABLED = "trust_enabled";
    public static final String SITE_CONFIG_TRUST_TITLE = "trust_title";
    public static final String SITE_CONFIG_TRUST_FEATURES = "trust_features";
    public static final String SITE_CONFIG_FOOTER_ABOUT = "footer_about";
    public static final String SITE_CONFIG_FOOTER_COLUMNS = "footer_columns";
    public static final String SITE_CONFIG_FOOTER_SUBSCRIBE_TEXT = "footer_subscribe_text";
    public static final String SITE_CONFIG_FOOTER_SOCIAL = "footer_social";
    public static final String SITE_CONFIG_FOOTER_PAYMENT_ICONS = "footer_payment_icons";

    /** Default values for site config keys — used when key is missing from DB */
    public static final java.util.Map<String, String> SITE_CONFIG_DEFAULTS = java.util.Map.ofEntries(
        java.util.Map.entry(SITE_CONFIG_PROMO_BAR_ENABLED, "true"),
        java.util.Map.entry(SITE_CONFIG_PROMO_BAR_MESSAGES, "[{\"text\":\"新用户立减10元！注册即享优惠\",\"bgColor\":\"#00676b\",\"textColor\":\"#ffffff\"},{\"text\":\"满99元免运费\",\"bgColor\":\"#0a8f94\",\"textColor\":\"#ffffff\"},{\"text\":\"限时特惠 低至5折\",\"bgColor\":\"#e74c3c\",\"textColor\":\"#ffffff\"}]"),
        java.util.Map.entry(SITE_CONFIG_HEADER_PHONE, "400-123-4567"),
        java.util.Map.entry(SITE_CONFIG_HERO_ENABLED, "true"),
        java.util.Map.entry(SITE_CONFIG_HERO_SLIDES, "[{\"image\":\"https://placehold.co/1920x460/00676b/ffffff?text=新品上市\",\"link\":\"/products\",\"title\":\"新品上市\",\"sort\":1},{\"image\":\"https://placehold.co/1920x460/0a8f94/ffffff?text=限时特惠\",\"link\":\"/products?sort=sales\",\"title\":\"限时特惠\",\"sort\":2},{\"image\":\"https://placehold.co/1920x460/2c3e50/ffffff?text=品质保证\",\"link\":\"/products\",\"title\":\"品质保证\",\"sort\":3}]"),
        java.util.Map.entry(SITE_CONFIG_HERO_INTERVAL, "4000"),
        java.util.Map.entry(SITE_CONFIG_POPULAR_TITLE, "热门推荐"),
        java.util.Map.entry(SITE_CONFIG_POPULAR_COUNT, "8"),
        java.util.Map.entry(SITE_CONFIG_POPULAR_SORT, "sales"),
        java.util.Map.entry(SITE_CONFIG_TRUST_ENABLED, "true"),
        java.util.Map.entry(SITE_CONFIG_TRUST_TITLE, "为什么选择我们"),
        java.util.Map.entry(SITE_CONFIG_TRUST_FEATURES, "[{\"icon\":\"TruckFilled\",\"title\":\"全国包邮\",\"description\":\"订单满99元享全国包邮\"},{\"icon\":\"CircleCheckFilled\",\"title\":\"品质保证\",\"description\":\"100%正品保障，假一赔十\"},{\"icon\":\"Clock\",\"title\":\"极速发货\",\"description\":\"下单后24小时内极速发货\"},{\"icon\":\"Service\",\"title\":\"贴心售后\",\"description\":\"7天无理由退换货，售后无忧\"}]"),
        java.util.Map.entry(SITE_CONFIG_FOOTER_ABOUT, "MyShop 致力于为您提供优质的购物体验，汇聚全球好物，让生活更美好。"),
        java.util.Map.entry(SITE_CONFIG_FOOTER_COLUMNS, "[{\"title\":\"购物指南\",\"links\":[{\"label\":\"如何下单\",\"url\":\"#\"},{\"label\":\"支付方式\",\"url\":\"#\"},{\"label\":\"配送说明\",\"url\":\"#\"}]},{\"title\":\"售后服务\",\"links\":[{\"label\":\"退换货政策\",\"url\":\"#\"},{\"label\":\"退款流程\",\"url\":\"#\"},{\"label\":\"投诉建议\",\"url\":\"#\"}]},{\"title\":\"关于我们\",\"links\":[{\"label\":\"公司介绍\",\"url\":\"#\"},{\"label\":\"联系我们\",\"url\":\"#\"},{\"label\":\"加入我们\",\"url\":\"#\"}]},{\"title\":\"关注我们\",\"links\":[{\"label\":\"微博\",\"url\":\"#\"},{\"label\":\"微信公众号\",\"url\":\"#\"},{\"label\":\"小红书\",\"url\":\"#\"}]}]"),
        java.util.Map.entry(SITE_CONFIG_FOOTER_SUBSCRIBE_TEXT, "订阅我们的资讯，获取最新优惠信息"),
        java.util.Map.entry(SITE_CONFIG_FOOTER_SOCIAL, "[{\"platform\":\"微信\",\"url\":\"#\",\"icon\":\"ChatDotSquare\"},{\"platform\":\"微博\",\"url\":\"#\",\"icon\":\"Share\"}]"),
        java.util.Map.entry(SITE_CONFIG_FOOTER_PAYMENT_ICONS, "[{\"name\":\"微信支付\",\"icon\":\"Wallet\"},{\"name\":\"支付宝\",\"icon\":\"Money\"},{\"name\":\"银联\",\"icon\":\"CreditCard\"}]")
    );

    // ========== Page Config Defaults ==========
    public static final String PAGE_CONFIG_HOME_DEFAULT = "{\"hero_enabled\":true,\"showcase_enabled\":true,\"showcase_categories\":[],\"popular_enabled\":true,\"trust_enabled\":true,\"banners\":[],\"sections\":[{\"type\":\"category_showcase\",\"title\":\"热门分类\",\"categoryIds\":[]},{\"type\":\"product_grid\",\"title\":\"推荐商品\",\"displayCount\":8,\"sortBy\":\"sales\"}]}";
    public static final String PAGE_CONFIG_PRODUCT_LIST_DEFAULT = "{\"displayMode\":\"grid\",\"pageSize\":20,\"showSidebar\":true,\"showFilters\":[\"category\",\"price\",\"keyword\"],\"defaultSort\":\"newest\",\"sidebarPosition\":\"left\",\"showSalesCount\":true,\"showOriginalPrice\":true}";
    public static final String PAGE_CONFIG_PRODUCT_DETAIL_DEFAULT = "{\"showBreadcrumb\":true,\"showRelatedProducts\":true,\"relatedCount\":4,\"imageDisplayMode\":\"thumbnail\",\"showSalesCount\":true,\"showShareButtons\":true}";
    public static final String PAGE_CONFIG_CART_DEFAULT = "{\"showRelatedProducts\":true,\"promotionText\":\"满99元免运费\",\"showQuantityDiscount\":false,\"showCouponInput\":true,\"showShippingEstimate\":false,\"relatedProductsEnabled\":true}";
    public static final String PAGE_CONFIG_PAYMENT_DEFAULT = "{\"paymentMethods\":[\"alipay\",\"wechat\"],\"instructionText\":\"请在15分钟内完成支付\",\"showOrderSummary\":true}";
    public static final String PAGE_CONFIG_THANK_YOU_DEFAULT = "{\"message\":\"感谢您的购买！\",\"subMessage\":\"我们会尽快为您发货\",\"buttonText\":\"继续购物\",\"buttonLink\":\"/products\",\"autoRedirectSeconds\":10}";

    private Constants() {}
}
