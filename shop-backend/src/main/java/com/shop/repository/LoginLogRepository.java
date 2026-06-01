package com.shop.repository;

import com.shop.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.ip = :ip AND l.status <> 'SUCCESS' AND l.createTime > :since")
    long countFailedByIpSince(String ip, LocalDateTime since);
}
