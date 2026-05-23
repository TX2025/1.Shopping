package com.shop.service;

import java.util.Map;

public interface SiteConfigService {
    Map<String, String> getAllAsMap();
    void updateAll(Map<String, String> values);
}
