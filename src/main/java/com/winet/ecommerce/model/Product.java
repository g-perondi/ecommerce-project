package com.winet.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@NotNull
	@NotBlank
	@Size(min = 2)
	private String productName;

	@Size(min = 6)
	private String description;

	@PositiveOrZero
	private Integer quantity;

	@Positive
	private BigDecimal price;

	private BigDecimal specialPrice;

	@PositiveOrZero
	private Double discount;

	private String image;

}
