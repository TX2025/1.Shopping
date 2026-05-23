package com.shop.service.impl;

import com.shop.common.Constants;
import com.shop.entity.SiteConfig;
import com.shop.repository.SiteConfigRepository;
import com.shop.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SiteConfigRepository siteConfigRepository;

    @Override
    public Map<String, String> getAllAsMap() {
        Map<String, String> result = new LinkedHashMap<>(Constants.SITE_CONFIG_DEFAULTS);
        for (SiteConfig sc : siteConfigRepository.findAll()) {
            result.put(sc.getConfigKey(), sc.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional
    public void updateAll(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            siteConfigRepository.findByConfigKey(entry.getKey()).ifPresent(sc -> {
                sc.setConfigValue(entry.getValue());
                siteConfigRepository.save(sc);
            });
        }
    }
}
