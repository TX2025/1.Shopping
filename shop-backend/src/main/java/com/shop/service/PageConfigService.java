package com.shop.service;

import com.shop.entity.PageConfig;

import java.util.Map;

public interface PageConfigService {
    PageConfig getConfig(String type);
    void updateConfig(String type, String json);
    Map<String, Object> getSchema(String type);
}
