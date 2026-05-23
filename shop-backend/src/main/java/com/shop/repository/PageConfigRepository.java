package com.shop.repository;

import com.shop.entity.PageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PageConfigRepository extends JpaRepository<PageConfig, Long> {
    Optional<PageConfig> findByPageType(String pageType);
}
