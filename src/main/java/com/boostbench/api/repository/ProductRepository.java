package com.boostbench.api.repository;

import com.boostbench.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCreatedAtAfter(LocalDateTime date);
    List<Product> findByDiscountGreaterThan(BigDecimal discount); // New method for special offers
}