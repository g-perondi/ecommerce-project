package com.winet.ecommerce.repository;

import com.winet.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	boolean existsByProductName(String productName);

	Page<Product> findByProductNameContainsIgnoreCase(String keyword, Pageable pageable);

	Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

}
