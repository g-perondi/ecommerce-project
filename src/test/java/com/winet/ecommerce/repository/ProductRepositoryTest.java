package com.winet.ecommerce.repository;

import com.winet.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

	@Autowired
	ProductRepository productRepository;

	@BeforeEach
	void setUp() {
		List<Product> products = List.of(
				new Product(null, "Laptop", "description", new BigDecimal("1200.00"), BigDecimal.ZERO, 0.0, "default.png"),
				new Product(null, "Smartphone", "description", new BigDecimal("800.00"), BigDecimal.ZERO, 0.0, "default.png"),
				new Product(null, "Tablet", "description", new BigDecimal("500.00"), BigDecimal.ZERO, 0.0, "default.png")
		);
		productRepository.saveAll(products);
	}

	@Test
	void testExistsByProductName_BasicScenario() {
		assertTrue(productRepository.existsByProductName("Laptop"));
		assertFalse(productRepository.existsByProductName("TV"));
	}

//	@Test
//	void testFindByProductNameContainsIgnoreCase_TrueScenario() {
//		Pageable pageable = PageRequest.of(0, 10);
//		Page<Product> productPage = productRepository.findByProductNameContainsIgnoreCase("smart", pageable);
//
//		assertTrue(productPage.hasContent());
//		assertTrue(productPage.getContent().get(0).getProductName().toLowerCase().contains("smart"));
//	}

//	@Test
//	void testFindByProductNameContainsIgnoreCase_FalseScenario() {
//		Pageable pageable = PageRequest.of(0, 10);
//		Page<Product> productPage = productRepository.findByProductNameContainsIgnoreCase("apple", pageable);
//
//		assertFalse(productPage.hasContent());
//	}

}
