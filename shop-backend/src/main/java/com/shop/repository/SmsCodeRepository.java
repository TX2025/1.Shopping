package com.shop.repository;

import com.shop.entity.SmsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

    Optional<SmsCode> findTopByPhoneAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreateTimeDesc(
            String phone, String type, LocalDateTime now);

    @Query("SELECT COUNT(s) FROM SmsCode s WHERE s.phone = :phone AND s.createTime > :since")
    long countByPhoneSince(String phone, LocalDateTime since);

    @Query("SELECT COUNT(s) FROM SmsCode s WHERE s.phone = :phone AND s.createTime > :since AND s.type = :type")
    long countByPhoneAndTypeSince(String phone, String type, LocalDateTime since);
}
