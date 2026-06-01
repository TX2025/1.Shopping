package com.shop.repository;

import com.shop.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {
}
